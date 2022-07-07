package flashcards

import java.io.File
import kotlin.random.Random

class FlashCards(args: Array<String>) {

    private var running = true
    private val cards = linkedMapOf<String, String>()
    private val stats = linkedMapOf<String, Int>()

    private fun add() {
        println("The card:")
        val term = readln()
        require(!cards.containsKey(term)) { "The card \"$term\" already exists." }
        println("The definition of the card:")
        val definition  = readln()
        require(!cards.containsValue(definition)) { "The definition \"$definition\" already exists." }
        cards[term] = definition
        println("The pair (\"$term\":\"$definition\") has been added.")
    }

    private fun remove() {
        println("Which card?")
        val term = readln()
        require (cards.remove(term) != null) { "Can't remove \"$term\": there is no such card." }
        stats.remove(term)
        println("The card has been removed.")
    }

    private fun askFileName(): String {
        println("File name:")
        return(readln())
    }

    private fun import(fileName: String? = null) {
        val file = File(fileName ?: askFileName())
        require(file.exists()) { "File not found." }
        val lines = file.readLines()
        for (i in lines.indices step 3) {
            val (term, definition, errors) =  Triple(lines[i], lines[i + 1], lines [i + 2].toInt())
            cards[term] = definition
            if (errors == 0) {
                stats.remove(term)
            } else {
                stats[term] = errors
            }
        }
        println("${lines.size / 3} cards have been loaded.")
    }

    private fun export(fileName: String? = null) {
        val file = File(fileName ?: askFileName())
        file.writeText("")
        cards.forEach {
            file.appendText("${it.key}\n")
            file.appendText("${it.value}\n")
            file.appendText("${stats.getOrDefault(it.key, 0)}\n")
        }
        println("${cards.size} cards have been saved.")
    }

    private fun ask() {
        require(cards.size > 0) { "There are no cards." }
        println("How many times to ask?")
        val times = readln().toInt()
        repeat(times) {
            val n = Random.nextInt(0, cards.size)
            checkCard(cards.asIterable().elementAt(n))
        }
    }

    private fun checkCard(card: Map.Entry<String, String>) {
        fun getTerm(definition: String) =
            cards.filterValues { it == definition }.keys.first()

        val (term, definition) = card
        println("Print the definition of \"${term}\":")
        val answer = readln()
        if (answer == definition) {
            println("Correct!")
            return
        }
        stats[term] = stats.getOrDefault(term, 0) + 1
        if (cards.containsValue(answer)) {
            val otherTerm = getTerm(answer)
            println("Wrong. The right answer is \"$definition\", " +
                    "but your definition is correct for \"$otherTerm\".")
        } else {
            println("Wrong. The right answer is \"$definition\".")
        }
    }

    private val logLines = mutableListOf<String>()

    private fun println(line: String?) {
        if (line != null) {
            logLines.add(line)
            kotlin.io.println(line)
        }
    }

    private fun readln(): String {
        val line = kotlin.io.readln()
        logLines.add(line)
        return line
    }

    private fun logSave() {
        val file = File(askFileName())
        file.writeText(logLines.joinToString("\n"))
        println("The log has been saved.")
    }

    private fun hardest() {
        require(stats.size > 0) { "There are no cards with errors." }
        val maxErrors = stats.values.maxOrNull()
        val hardestStats = stats.filterValues { it == maxErrors }
        if (hardestStats.size == 1) {
            println("The hardest card is \"${hardestStats.keys.first()}\". " +
                    "You have \"$maxErrors\" errors answering it.")
        } else {
            println("The hardest cards are \"${hardestStats.keys.joinToString("\", \"")}\". " +
                    "You have \"$maxErrors\" errors answering them.")
        }
    }

    private fun reset() {
        stats.clear()
        println("Card statistics have been reset.")
    }

    private fun run () {
        val menu = linkedMapOf(
            "add" to { add() },
            "remove" to { remove() },
            "import" to { import() },
            "export" to { export() },
            "ask" to { ask() },
            "exit" to { running = false },
            "log" to { logSave() },
            "hardest card" to { hardest() },
            "reset stats" to { reset() },
        )
        val menuPrompt = "Input the action (${menu.keys.joinToString(", ")}):"

        while (running) {
            println(menuPrompt)
            try {
                menu[readln()]?.let { it() } ?: throw(RuntimeException("Unknown action."))
            } catch (e: RuntimeException) { println(e.message) }
        }
        println("Bye bye!")
    }

    init {
        val importOption = args.indexOf("-import") + 1
        if (importOption in 1..args.lastIndex) import(args[importOption])
        run()
        val exportOption = args.indexOf("-export") + 1
        if (exportOption in 1..args.lastIndex) export(args[exportOption])
    }
}

fun main(args: Array<String>) {
    FlashCards(args)
}
