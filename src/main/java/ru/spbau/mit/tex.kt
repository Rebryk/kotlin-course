package ru.spbau.mit

import java.io.OutputStream

interface Element {
    fun render(builder: StringBuilder)
}

@DslMarker
annotation class TeXTagMarker

class TextElement(private val text: String) : Element {
    override fun render(builder: StringBuilder) {
        builder.append("$text\n")
    }
}

@TeXTagMarker
abstract class SimpleTag(
        val name: String,
        val arguments: List<String> = arrayListOf()
) : Element {
    protected fun renderArguments(): String {
        return if (arguments.isNotEmpty()) {
            arguments.joinToString(separator = "}{", prefix = "{", postfix = "}")
        } else {
            ""
        }
    }

    override fun render(builder: StringBuilder) {
        builder.append("\\$name${renderArguments()}\n")
    }
}

abstract class AdvancedTag(
        name: String,
        arguments: List<String>,
        private val options: Map<String, String> = hashMapOf()
) : SimpleTag(name, arguments) {
    protected fun renderOptions(): String {
        return if (options.isNotEmpty()) {
            options.entries.joinToString(prefix = "[", postfix = "]", transform = { "${it.key} = ${it.value}" })
        } else {
            ""
        }
    }
}

abstract class LeftAdvancedTag(
        name: String,
        arguments: List<String>,
        options: Map<String, String> = hashMapOf()
) : AdvancedTag(name, arguments, options) {
    override fun render(builder: StringBuilder) {
        builder.append("\\$name${renderOptions()}${renderArguments()}\n")
    }
}

abstract class RightAdvancedTag(
        name: String,
        arguments: List<String>,
        options: Map<String, String> = hashMapOf()
) : AdvancedTag(name, arguments, options) {
    override fun render(builder: StringBuilder) {
        builder.append("\\$name${renderArguments()}${renderOptions()}\n")
    }
}

abstract class ContentTag(
        name: String,
        arguments: List<String> = listOf(),
        options: Map<String, String> = hashMapOf()
) : AdvancedTag(name, arguments, options) {
    val children = arrayListOf<Element>()

    fun customTag(name: String,
                  arguments: List<String>,
                  vararg options: Pair<String, String>,
                  init: CustomTag.() -> Unit) = initTag(CustomTag(name, arguments, *options), init)

    operator fun String.unaryPlus() {
        children.add(TextElement(this))
    }

    override fun render(builder: StringBuilder) {
        builder.append("\\begin{$name}${renderArguments()}${renderOptions()}\n")
        children.forEach { it.render(builder) }
        builder.append("\\end{$name}\n")
    }

    protected fun <T : Element> initTag(tag: T, init: T.() -> Unit = {}): T {
        tag.init()
        children.add(tag)
        return tag
    }
}

abstract class TeXContentTag(
        name: String,
        arguments: List<String> = listOf(),
        options: Map<String, String> = hashMapOf()
) : ContentTag(name, arguments, options) {
    fun itemize(vararg options: Pair<String, String>,
                init: Itemize.() -> Unit) = initTag(Itemize(options.toList()), init)

    fun enumerate(vararg options: Pair<String, String>,
                  init: Enumerate.() -> Unit) = initTag(Enumerate(options.toList()), init)

    fun frame(frameTitle: String,
              vararg options: Pair<String, String>,
              init: Frame.() -> Unit) = initTag(Frame(frameTitle, options.toList()), init)

    fun math(vararg options: Pair<String, String>,
             init: Math.() -> Unit) = initTag(Math(options.toList()), init)

    fun left(init: LeftAlignment.() -> Unit) = initTag(LeftAlignment(), init)

    fun right(init: RightAlignment.() -> Unit) = initTag(RightAlignment(), init)

    fun center(init: CenterAlignment.() -> Unit) = initTag(CenterAlignment(), init)
}

class CustomTag(
        name: String,
        arguments: List<String> = listOf(),
        vararg options: Pair<String, String>
) : TeXContentTag(name, arguments = arguments, options = options.toMap())

class Document : TeXContentTag("document")

class DocumentClass(
        clazz: String,
        options: List<Pair<String, String>>
) : LeftAdvancedTag("documentclass", arguments = listOf(clazz), options = options.toMap())

class UsePackage(
        packages: List<String>,
        options: List<Pair<String, String>> = listOf()
) : LeftAdvancedTag("usepackage", arguments = listOf(packages.joinToString()), options = options.toMap())

class Title(title: String) : SimpleTag("title", arguments = listOf(title))

class Author(author: String) : SimpleTag("author", arguments = listOf(author))

class Date(date: String) : SimpleTag("date", arguments = listOf(date))

class FrameTitle(title: String) : SimpleTag("frametitle", arguments = listOf(title))

class Item : TeXContentTag("item") {
    override fun render(builder: StringBuilder) {
        builder.append("\\$name${renderArguments()}${renderOptions()}\n")
        children.forEach { it.render(builder) }
    }
}

class Itemize(options: List<Pair<String, String>>) : ContentTag("itemize", options = options.toMap()) {
    fun item(init: Item.() -> Unit) = initTag(Item(), init)
}

class Enumerate(options: List<Pair<String, String>>) : ContentTag("enumerate", options = options.toMap()) {
    fun item(init: Item.() -> Unit) = initTag(Item(), init)
}

class Math(options: List<Pair<String, String>>) : ContentTag("displaymath", options = options.toMap())

class LeftAlignment : TeXContentTag("left")

class RightAlignment : TeXContentTag("right")

class CenterAlignment : TeXContentTag("center")

class Frame(
        frameTitle: String,
        options: List<Pair<String, String>>
) : TeXContentTag("frame", options = options.toMap()) {
    init {
        initTag(FrameTitle(frameTitle))
    }
}

class TeXDocument : ContentTag("") {
    fun documentClass(clazz: String,
                      vararg options: Pair<String, String>) = initTag(DocumentClass(clazz, options.toList()))

    fun usepackage(name: String) = initTag(UsePackage(listOf(name)))

    fun usepackage(vararg packages: String) = initTag(UsePackage(packages.toList()))

    fun usepackage(name: String,
                   vararg options: Pair<String, String>) = initTag(UsePackage(listOf(name), options.toList()))

    fun title(title: String) = initTag(Title(title))

    fun author(author: String) = initTag(Author(author))

    fun date(date: String) = initTag(Date(date))

    fun document(init: Document.() -> Unit) = initTag(Document(), init)

    override fun render(builder: StringBuilder) {
        children.forEach { it.render(builder) }
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder)
        return builder.toString()
    }

    fun toOutputStream(output: OutputStream) {
        output.write(toString().toByteArray())
    }
}

fun TeX(init: TeXDocument.() -> Unit) = TeXDocument().apply(init)