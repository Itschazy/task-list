package tasklist

import kotlinx.datetime.*
import kotlin.system.exitProcess
import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File

const val INPUT_TASKS_MESSAGE = "Input a new task (enter a blank line to end):"
const val WELCOMING_MESSAGE = "Input an action (add, print, edit, delete, end):"
const val TOP_OF_TABLE = "+----+------------+-------+---+---+--------------------------------------------+\n" +
        "| N  |    Date    | Time  | P | D |                   Task                     |\n" +
        "+----+------------+-------+---+---+--------------------------------------------+"
const val NEW_LINE = "|    |            |       |   |   |"
const val BREAK_LINE = "+----+------------+-------+---+---+--------------------------------------------+"

val jsonFile = File("tasklist.json")

class TaskList {

    val taskList: MutableList<Tasks> = mutableListOf()

    data class Tasks(
        var priority: String = "",
        var date: String = "",
        var time: String = "",
        var due: String = "",
        var listOfTasks: MutableList<String> = mutableListOf()
    )

    fun saveToJson() {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val type = Types.newParameterizedType(MutableList::class.java, Tasks::class.java)
        val tasksAdapter = moshi.adapter<MutableList<Tasks>>(type)
        val jsonContent = tasksAdapter.toJson(taskList)
        jsonFile.writeText(jsonContent)
    }

    fun readFromJson() {
        if (jsonFile.exists()) {
            val content = jsonFile.readText()?.trimIndent()
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()
            val type = Types.newParameterizedType(MutableList::class.java, Tasks::class.java)
            val tasksAdapter = moshi.adapter<MutableList<Tasks>>(type)
            tasksAdapter.fromJson(jsonFile.readText())?.let(taskList::addAll)
        }
    }


    fun getOverdue(task: Tasks) {
        val taskDate = task.date.toLocalDate()
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskDate)
        task.due = when {
            numberOfDays == 0 -> "T"
            numberOfDays < 0 -> "O"
            else -> "I"
        }
    }

    fun getPriority(task: Tasks) {
        val regex = "[chnl]".toRegex()
        while (true) {
            println("Input the task priority (C, H, N, L):")
            val input = readln().trim().lowercase()
            if (input.matches(regex)) {
                task.priority = input.uppercase()
                break
            }
        }
    }

    fun getDate(task: Tasks) {
        val regex = """\d{4}-\d{1,2}-\d{1,2}""".toRegex()
        while (true) {
            println("Input the date (yyyy-mm-dd):")
            val input = readln().trim().lowercase()
            if (input.matches(regex)) {
                val (yyyy, mm, dd) = input.split("-").map { it.toInt() }
                try {
                    task.date = LocalDate(yyyy, mm, dd).toString()
                    break
                } catch (e: IllegalArgumentException) {
                    println("The input date is invalid")
                }
            } else {
                println("The input date is invalid")
            }
        }
    }

    fun getTime(task: Tasks) {
        val regex = """\d{1,2}:\d{1,2}""".toRegex()
        while (true) {
            println("Input the time (hh:mm):")
            val input = readln().trim()
            if (input.matches(regex)) {
                val (hh, mm) = input.split(":").map { it.toInt() }
                try {
                    task.time = LocalTime(hh, mm).toString()
                    break
                } catch (e: IllegalArgumentException) {
                    println("The input time is invalid")
                }
            } else {
                println("The input time is invalid")
            }
        }
    }

    fun getTasks(task: Tasks) {
        println(INPUT_TASKS_MESSAGE)
        val currentTaskList = mutableListOf<String>()
        while (true) {
            val input = readln().trim()
            if (input.isNotEmpty()) {
                currentTaskList.add(input)
            } else if (currentTaskList.isEmpty()) {
                println("The task is blank")
                break
            } else break
        }
        if (currentTaskList.isNotEmpty()) {
            task.listOfTasks = currentTaskList
        }
    }

    fun deleteTask() {
        printTasks()
        if (taskList.isNotEmpty()) {
            while (true) {
                println("Input the task number (1-${taskList.size}):")
                try {
                    val input = readln().toInt()
                    if (input in 1..taskList.size) {
                        taskList.removeAt(input - 1)
                        println("The task is deleted")
                        break
                    } else {
                        println("Invalid task number")
                    }
                } catch (e: Exception) {
                    println("Invalid task number")
                }
            }
        }
    }

    fun editTask() {
        printTasks()
        var flag = true
        if (taskList.isNotEmpty()) {
            while (flag) {
                println("Input the task number (1-${taskList.size}):")
                try {
                    val input = readln().trim().toInt()
                    if (input in 1..taskList.size) {
                        val currentTask = taskList[input - 1]
                        while (flag) {
                            println("Input a field to edit (priority, date, time, task):")
                            when (readln().trim().lowercase()) {
                                "priority" -> {
                                    getPriority(currentTask)
                                    println("The task is changed")
                                    flag = false
                                }
                                "date" -> {
                                    getDate(currentTask)
                                    println("The task is changed")
                                    flag = false
                                }
                                "time" -> {
                                    getTime(currentTask)
                                    println("The task is changed")
                                    flag = false
                                }
                                "task" -> {
                                    getTasks(currentTask)
                                    println("The task is changed")
                                    flag = false
                                }
                                else -> println("Invalid field")
                            }
                        }
                    } else println("Invalid task number")
                } catch (e: Exception) {
                    println("Invalid task number")
                }
            }
        }
    }

    fun addTasks() {
        val task = Tasks()
        getPriority(task)
        getDate(task)
        getTime(task)
        getOverdue(task)
        getTasks(task)
        taskList.add(task)
    }

    fun showMenu() {
        readFromJson()
        while (true) {
            println(WELCOMING_MESSAGE)
            when (readln().lowercase()) {
                "add" -> addTasks()
                "print" -> printTasks()
                "delete" -> deleteTask()
                "edit" -> editTask()
                "end" -> {
                    println("Tasklist exiting!")
                    saveToJson()
                    exitProcess(0)
                }
                else -> println("The input action is invalid")
            }
        }
    }

    fun printTasks() {
        if (taskList.isNotEmpty()) {
            println(TOP_OF_TABLE)
            for (i in 1..taskList.size) {
                var spaces = if (i > 9) " " else "  "
                var priority = when (taskList[i - 1].priority) {
                    "C" -> "\u001B[101m \u001B[0m"
                    "H" -> "\u001B[103m \u001B[0m"
                    "N" -> "\u001B[102m \u001B[0m"
                    "L" -> "\u001B[104m \u001B[0m"
                    else -> continue
                }
                var due = when (taskList[i - 1].due) {
                    "I" -> "\u001B[102m \u001B[0m"
                    "T" -> "\u001B[103m \u001B[0m"
                    "O" -> "\u001B[101m \u001B[0m"
                    else -> continue
                }
                print("| $i  | ${taskList[i - 1].date} | ${taskList[i - 1].time} | $priority | $due |")
                for (j in 0 until taskList[i - 1].listOfTasks.size) {
                    var chunkedList = taskList[i - 1].listOfTasks[j].chunked(44)
                    for (element in chunkedList) {
                        var spaces = " ".repeat(44 - element.length)
                        print("${element}$spaces|\n")
                        if ((j != taskList[i - 1].listOfTasks.size - 1) || (chunkedList.indexOf(element) != chunkedList.lastIndex)) {
                            print(NEW_LINE)
                        }
                    }
                }
                println(BREAK_LINE)
            }
        } else println("No tasks have been input")
    }
}

fun main() {
    val taskList = TaskList()
    taskList.showMenu()
}


