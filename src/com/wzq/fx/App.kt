package com.wzq.fx

import javafx.application.Application
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.layout.VBox
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.lang.Exception


class App : Application() {
    override fun start(primaryStage: Stage) {
        val root = ui(primaryStage)
        primaryStage.title = "refactor file name"
        primaryStage.scene = Scene(root, 400.00, 600.00)
        primaryStage.show()
    }

    private fun ui(primaryStage: Stage): VBox {
        val dirSelector = DirectoryChooser()
        return VBox(8.0).apply {
            padding = Insets(16.0, 16.0, 16.0, 16.0)
            val desc = Label("当前路径：")
            val selectFile = Button("选择文件夹")
            val listView: ListView<String> = ListView()
            val reset = Button("重置")
            var currentFile: File? = null
            selectFile.setOnAction {
                val dir = dirSelector.showDialog(primaryStage) ?: return@setOnAction
                currentFile = dir
                desc.text = "当前路径：${dir.absolutePath}"
                updateList(doRefactor(dir), listView)
            }
            reset.setOnAction {
                updateList(reset(currentFile), listView)
            }
            val export = Button("导出")
            export.setOnAction {
                val e = DirectoryChooser().showDialog(primaryStage)
                val data = arrayListOf<String>()
                currentFile?.listFiles()?.forEach {
                    if (!it.isHidden) data.add(it.name)
                }
                data.sort()
                if (data.isNotEmpty()) exportList(e, data)
            }
            children.add(desc)
            children.add(selectFile)
            children.add(listView)
            children.add(reset)
            children.add(export)
        }
    }

    fun exportList(file: File?, data: List<String>) {
        if (file!=null && file.exists() && file.isDirectory) {
            try {
                val sdp = File(file, "sdp的清单.txt")
                if (!sdp.exists()){
                    sdp.createNewFile()
                }
                val fw = FileWriter(sdp)
                val buffer = BufferedWriter(fw)
                data.forEach {
                    buffer.write(it)
                    buffer.newLine()
                }
                buffer.flush()
                buffer.close()
                fw.close()
            }catch (e: Exception){}
        }
    }

    private fun updateList(data: List<String>?, listView: ListView<String>) {
        val strList = FXCollections.observableArrayList(data)
        listView.items = strList
    }

    private fun reset(cPath: File?): List<String> {
        val list = arrayListOf<String>()
        if (cPath != null && cPath.exists() && cPath.isDirectory) {
            cPath.listFiles()?.forEach {
                if(!it.isHidden){
                    val newName = it.name.substringAfter("-", "")
                    if (newName.isNotEmpty()) {
                        val newFile = File(it.parent + File.separator + it.name.substringAfter("-", ""))
                        it.renameTo(newFile)
                        list.add(newFile.name)
                    }
                }

            }
        }
        println(list)
        return list
    }

    private fun doRefactor(file: File?): List<String> {
        val list = arrayListOf<String>()
        if (file != null && file.exists() && !file.isDirectory) return list
        val len = file?.listFiles()?.size ?: 0
        file?.listFiles()?.forEachIndexed { index, it ->
            if(!it.isHidden) {

                val p = formatNum(index + 1, len)
                val newFile = File("${it.parent}${File.separator}$p-${it.name}")
                list.add(newFile.name)
                it.renameTo(newFile)
            }
        }
        return list
    }

    fun formatNum(num: Int, len: Int): String {
        val s = len.toString().length
        return String.format("%0${s}d", num)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(App::class.java, *args)
        }
    }
}