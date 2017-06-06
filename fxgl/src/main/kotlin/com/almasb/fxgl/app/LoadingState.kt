/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.app

import com.almasb.fxgl.annotation.AddCollisionHandler
import com.almasb.fxgl.annotation.SetEntityFactory
import com.almasb.fxgl.entity.EntityFactory
import com.almasb.fxgl.physics.CollisionHandler
import com.almasb.fxgl.saving.DataFile
import com.almasb.fxgl.scene.LoadingScene
import com.almasb.fxgl.scene.SceneFactory
import com.google.inject.Inject
import com.google.inject.Singleton
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import javafx.concurrent.Task
import java.util.*

/**
 * Initializes game aspects: assets, game, physics, UI, etc.
 * This task is rerun every time the game application is restarted.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
@Singleton
internal class LoadingState
@Inject private constructor(private val app: GameApplication,
                            sceneFactory: SceneFactory) : AppState(sceneFactory.newLoadingScene()) {

    var dataFile = DataFile.EMPTY

    private var loadingFinished = false

    override fun onEnter(prevState: State) {

        val initTask = InitAppTask(FXGL.getApp(), dataFile)
        initTask.setOnSucceeded {
            loadingFinished = true
        }

        dataFile = DataFile.EMPTY

        (scene as LoadingScene).bind(initTask)

        FXGL.getExecutor().execute(initTask)
    }

    override fun onUpdate(tpf: Double) {
        if (loadingFinished) {
            app.stateMachine.startPlay()
            loadingFinished = false
        }
    }

    private class InitAppTask(private val app: GameApplication, private val dataFile: DataFile) : Task<Void>() {

        companion object {
            private val log = FXGL.getLogger(InitAppTask::class.java)
        }

        override fun call(): Void? {
            val start = System.nanoTime()

            log.debug("Clearing game world")
            app.gameWorld.reset()

            update("Initializing Assets", 0)
            app.initAssets()

            update("Initializing Game", 1)

            log.debug("Clearing game state")
            app.gameState.clear()

            val vars = hashMapOf<String, Any>()
            app.initGameVars(vars)
            vars.forEach { name, value -> app.gameState.put(name, value) }

            val annotationMap = scanForAnnotations()

            annotationMap[SetEntityFactory::class.java]?.let {
                if (it.isNotEmpty())
                    app.gameWorld.setEntityFactory(FXGL.getInstance(it[0]) as EntityFactory)
            }

            if (dataFile === DataFile.EMPTY)
                app.initGame()
            else
                app.loadState(dataFile)

            update("Initializing Physics", 2)
            app.initPhysics()

            annotationMap[AddCollisionHandler::class.java]?.let {
                it.forEach {
                    val handler = FXGL.getInstance(it) as CollisionHandler

                    log.debug("@Add $handler")

                    app.physicsWorld.addCollisionHandler(handler)
                }
            }

            update("Initializing UI", 3)
            app.initUI()

            update("Initialization Complete", 4)

            log.infof("Game initialization took: %.3f sec", (System.nanoTime() - start) / 1000000000.0)

            return null
        }

        private fun update(message: String, step: Int) {
            log.debug(message)
            updateMessage(message)
            updateProgress(step.toLong(), 4)
        }

        override fun failed() {
            Thread.getDefaultUncaughtExceptionHandler()
                    .uncaughtException(Thread.currentThread(), exception ?: RuntimeException("Initialization failed"))
        }

        private fun scanForAnnotations(): Map<Class<*>, List<Class<*>>> {
            val map = hashMapOf<Class<*>, ArrayList<Class<*>>>()

            if (app.javaClass.`package` != null) {
                // only scan the appropriate package (package of the "App") and its subpackages
                val scanner = FastClasspathScanner(app.javaClass.`package`.name)

                map[SetEntityFactory::class.java] = arrayListOf()
                scanner.matchClassesWithAnnotation(SetEntityFactory::class.java, {
                    log.debug("@SetEntityFactory: $it")
                    map[SetEntityFactory::class.java]!!.add(it)
                })

                map[AddCollisionHandler::class.java] = arrayListOf()
                scanner.matchClassesWithAnnotation(AddCollisionHandler::class.java, {
                    log.debug("@AddCollisionHandler: $it")
                    map[AddCollisionHandler::class.java]!!.add(it)
                })

                scanner.scan()
            } else {
                log.warning("${app.javaClass.simpleName} has no package. Disabling annotations processing")
            }

            return map
        }
    }
}