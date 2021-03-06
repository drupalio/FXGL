/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.ui

import com.almasb.fxgl.app.FXGL
import com.almasb.fxgl.devtools.DeveloperTools
import javafx.scene.Cursor
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.layout.*
import javafx.scene.paint.Color

/**
 * Adapted from jfxtras-window
 *
 * TODO: clean up
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
open class MDIWindow : Region() {

    internal enum class ResizeMode {
        NONE,
        TOP,
        LEFT,
        BOTTOM,
        RIGHT,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    private var mouseX: Double = 0.0
    private var mouseY: Double = 0.0
    private var nodeX = 0.0
    private var nodeY = 0.0
    private var dragging = false
    private var zoomable = true
    private var minScale = 0.1
    private var maxScale = 10.0
    private var scaleIncrement = 0.001

    private var resizeMode: ResizeMode = ResizeMode.NONE

    private var RESIZE_TOP: Boolean = false
    private var RESIZE_LEFT: Boolean = false
    private var RESIZE_BOTTOM: Boolean = false
    private var RESIZE_RIGHT: Boolean = false

    private var prevHeight = 0.0


    // CUSTOM

    private val header = FXGL.getUIFactory().newTextFlow()

    private val minimizeButton = Button("-")
    private val closeButton = Button("x")

    private val root = Pane()
    var contentPane: Pane = StackPane()
        set(value) {
            field = value

            root.children.set(1, value)

            if (value.width < width) {
                width = value.width
            }

//            value.widthProperty().addListener { observable, oldValue, newValue ->
//                if (width > newValue.toDouble()) {
//                    width = newValue.toDouble()
//                }
//            }
        }

    var isMinimized = false

    var canResize = true
    var canMove = true

    var canMinimize = true
        set(value) {
            field = value

            minimizeButton.isVisible = value
        }


    var canClose = true
        set(value) {
            field = value

            closeButton.isVisible = value
        }

    var title = "Title"
        set(value) {
            field = value

            updateTitle()
        }

    private fun updateTitle() {
        header.children.clear()
        header.append(title, Color.WHITE)
    }

    init {
        val control = this

        if (canMove) {

            setOnMousePressed { event ->
                val n = control

                val parentScaleX = n.getParent().localToSceneTransformProperty().getValue().getMxx()
                val parentScaleY = n.getParent().localToSceneTransformProperty().getValue().getMyy()

                mouseX = event.sceneX
                mouseY = event.sceneY

                nodeX = n.getLayoutX() * parentScaleX
                nodeY = n.getLayoutY() * parentScaleY

//            if (control.isMoveToFront()) {
//                control.toFront()
//            }
            }

            setOnMouseDragged { event ->
                val n = control

                val parentScaleX = n.getParent().localToSceneTransformProperty().getValue().getMxx()
                val parentScaleY = n.getParent().localToSceneTransformProperty().getValue().getMyy()

                val scaleX = n.localToSceneTransformProperty().getValue().getMxx()
                val scaleY = n.localToSceneTransformProperty().getValue().getMyy()

                val boundsInScene = control.localToScene(control.getBoundsInLocal())

                val sceneX = boundsInScene.getMinX()
                val sceneY = boundsInScene.getMinY()

                val offsetX = event.sceneX - mouseX
                val offsetY = event.sceneY - mouseY

                if (resizeMode == ResizeMode.NONE && control.canMove) {

                    nodeX += offsetX
                    nodeY += offsetY

                    val scaledX = nodeX * 1 / parentScaleX
                    val scaledY = nodeY * 1 / parentScaleY

                    val offsetForAllX = scaledX - n.getLayoutX()
                    val offsetForAllY = scaledY - n.getLayoutY()

                    n.setLayoutX(scaledX)
                    n.setLayoutY(scaledY)

                    dragging = true


                } else {

                    val width = n.getBoundsInLocal().getMaxX() - n.getBoundsInLocal().getMinX()
                    val height = n.getBoundsInLocal().getMaxY() - n.getBoundsInLocal().getMinY()

                    if (RESIZE_TOP) {
                        val insetOffset = getInsets().getTop() / 2

                        val yDiff = sceneY / parentScaleY + insetOffset - event.sceneY / parentScaleY

                        val newHeight = control.getPrefHeight() + yDiff

                        if (newHeight > control.minHeight(0.0)) {
                            control.setLayoutY(control.getLayoutY() - yDiff)
                            control.setPrefHeight(newHeight)
                        }
                    }
                    if (RESIZE_LEFT) {
                        val insetOffset = getInsets().getLeft() / 2

                        val xDiff = sceneX / parentScaleX + insetOffset - event.sceneX / parentScaleX

                        val newWidth = control.getPrefWidth() + xDiff

                        if (newWidth > Math.max(control.minWidth(0.0), control.contentPane.minWidth(0.0))) {
                            control.setLayoutX(control.getLayoutX() - xDiff)
                            control.setPrefWidth(newWidth)
                        } else {
                            //
                        }
                    }

                    if (RESIZE_BOTTOM) {
                        val insetOffset = getInsets().getBottom() / 2

                        val yDiff = (event.sceneY / parentScaleY
                                - sceneY / parentScaleY - insetOffset)

                        var newHeight = yDiff

                        newHeight = Math.max(
                                newHeight, control.minHeight(0.0))

                        if (newHeight < control.maxHeight(0.0)) {
                            control.setPrefHeight(newHeight)
                        }
                    }
                    if (RESIZE_RIGHT) {

                        val insetOffset = getInsets().getRight() / 2

                        val xDiff = (event.sceneX / parentScaleX
                                - sceneX / parentScaleY - insetOffset)

                        var newWidth = xDiff

                        newWidth = Math.max(
                                newWidth,
                                Math.max(control.contentPane.minWidth(0.0),
                                        control.minWidth(0.0)))

                        if (newWidth < control.maxWidth(0.0)) {
                            control.setPrefWidth(newWidth)
                        }
                    }
                }

                mouseX = event.sceneX
                mouseY = event.sceneY
            }

            setOnMouseClicked {
                dragging = false
            }

            setOnMouseMoved { t ->
                if (control.isMinimized || !control.canResize) {

                    RESIZE_TOP = false
                    RESIZE_LEFT = false
                    RESIZE_BOTTOM = false
                    RESIZE_RIGHT = false

                    resizeMode = ResizeMode.NONE

                    return@setOnMouseMoved
                }

                val n = control

                val parentScaleX = n.getParent().localToSceneTransformProperty().getValue().getMxx()
                val parentScaleY = n.getParent().localToSceneTransformProperty().getValue().getMyy()

                val scaleX = n.localToSceneTransformProperty().getValue().getMxx()
                val scaleY = n.localToSceneTransformProperty().getValue().getMyy()

                // TODO: getResizableBorderWidth()
                val border = 5.0 * scaleX

                val diffMinX = Math.abs(n.getLayoutBounds().getMinX() - t.x + getInsets().getLeft())
                val diffMinY = Math.abs(n.getLayoutBounds().getMinY() - t.y + getInsets().getTop())
                val diffMaxX = Math.abs(n.getLayoutBounds().getMaxX() - t.x - getInsets().getRight())
                val diffMaxY = Math.abs(n.getLayoutBounds().getMaxY() - t.y - getInsets().getBottom())

                val left = diffMinX * scaleX < Math.max(border, getInsets().getLeft() / 2 * scaleX)
                val top = diffMinY * scaleY < Math.max(border, getInsets().getTop() / 2 * scaleY)
                val right = diffMaxX * scaleX < Math.max(border, getInsets().getRight() / 2 * scaleX)
                val bottom = diffMaxY * scaleY < Math.max(border, getInsets().getBottom() / 2 * scaleY)

                RESIZE_TOP = false
                RESIZE_LEFT = false
                RESIZE_BOTTOM = false
                RESIZE_RIGHT = false

                if (left && !top && !bottom) {
                    n.setCursor(Cursor.W_RESIZE)
                    resizeMode = ResizeMode.LEFT
                    RESIZE_LEFT = true
                } else if (left && top && !bottom) {
                    n.setCursor(Cursor.NW_RESIZE)
                    resizeMode = ResizeMode.TOP_LEFT
                    RESIZE_LEFT = true
                    RESIZE_TOP = true
                } else if (left && !top && bottom) {
                    n.setCursor(Cursor.SW_RESIZE)
                    resizeMode = ResizeMode.BOTTOM_LEFT
                    RESIZE_LEFT = true
                    RESIZE_BOTTOM = true
                } else if (right && !top && !bottom) {
                    n.setCursor(Cursor.E_RESIZE)
                    resizeMode = ResizeMode.RIGHT
                    RESIZE_RIGHT = true
                } else if (right && top && !bottom) {
                    n.setCursor(Cursor.NE_RESIZE)
                    resizeMode = ResizeMode.TOP_RIGHT
                    RESIZE_RIGHT = true
                    RESIZE_TOP = true
                } else if (right && !top && bottom) {
                    n.setCursor(Cursor.SE_RESIZE)
                    resizeMode = ResizeMode.BOTTOM_RIGHT
                    RESIZE_RIGHT = true
                    RESIZE_BOTTOM = true
                } else if (top && !left && !right) {
                    n.setCursor(Cursor.N_RESIZE)
                    resizeMode = ResizeMode.TOP
                    RESIZE_TOP = true
                } else if (bottom && !left && !right) {
                    n.setCursor(Cursor.S_RESIZE)
                    resizeMode = ResizeMode.BOTTOM
                    RESIZE_BOTTOM = true
                } else {
                    n.setCursor(Cursor.DEFAULT)
                    resizeMode = ResizeMode.NONE
                }

                control.autosize()
            }
        }

        children.add(root)

        minimizeButton.setOnAction {
            isMinimized = !isMinimized

            if (control.prefHeight > 0.0) {
                prevHeight = control.prefHeight
            }

            control.prefHeight = if (isMinimized) 0.0 else prevHeight
        }

        closeButton.setOnAction {
            DeveloperTools.removeFromParent(this)
        }

        // TODO: redesign graphics and position
        minimizeButton.translateY = -25.0
        closeButton.translateX = 25.0
        closeButton.translateY = -25.0

        val box = HBox(header)
        box.translateY = -25.0
        box.styleClass.add("window-titlebar")

//        box.prefWidthProperty().addListener { observable, oldValue, newValue ->
//            println(newValue)
//        }

//        root.widthProperty().addListener { observable, oldValue, newValue ->
//            println("Root: $newValue")
//
//            if ( > newValue.toDouble()) {
//                width = newValue.toDouble()
//            }
//        }

        box.prefWidthProperty().bind(this.widthProperty())

        root.children.add(box)
        root.children.add(contentPane)
        root.children.addAll(minimizeButton, closeButton)

        root.layoutY = 25.0

        background = Background(BackgroundFill(Color.BLACK, null, null))
    }
}