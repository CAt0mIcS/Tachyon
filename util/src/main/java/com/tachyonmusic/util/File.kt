package com.tachyonmusic.util

import java.io.FileFilter
import java.io.FilenameFilter
import java.io.IOException
import java.io.Serializable
import java.net.MalformedURLException
import java.net.URI
import java.net.URL


/**
 * Mockk doesn't allow mocking [java.io.File] class. But we need to mock its constructor
 * for testing. Build [java.io.File] wrapper to allow for mocking
 */
class File(
    val raw: java.io.File
) : Serializable, Comparable<File> {

    constructor(pathname: String) : this(java.io.File(pathname))

    constructor(parent: String, child: String) : this(java.io.File(parent, child))

    constructor(uri: URI) : this(java.io.File(uri))

    val name: String
        get() = raw.name

    val parent: String?
        get() = raw.parent

    val parentFile: File?
        get() {
            return File(raw.parentFile ?: return null)
        }

    val path: String
        get() = raw.path

    val absolute: Boolean
        get() = raw.isAbsolute

    val absolutePath: String
        get() = raw.absolutePath

    val absoluteFile: File
        get() = File(raw.absoluteFile)

    val canonicalPath: String
        @Throws(IOException::class)
        get() = raw.canonicalPath

    val canonicalFile: File
        @Throws(IOException::class)
        get() = File(raw.canonicalFile)

    @Deprecated(
        """This method does not automatically escape characters that
      are illegal in URLs.  It is recommended that new code convert an
      abstract pathname into a URL by first converting it into a URI, via the
      {@link #toURI() toURI} method, and then converting the URI into a URL
      via the {@link java.net.URI#toURL() URI.toURL} method."""
    )
    @Throws(
        MalformedURLException::class
    )
    fun toURL(): URL = raw.toURL()

    fun toURI(): URI = raw.toURI()

    fun canRead(): Boolean = raw.canRead()

    fun canWrite(): Boolean = raw.canWrite()
    fun exists(): Boolean = raw.exists()

    val isDirectory: Boolean
        get() = raw.isDirectory

    val isFile: Boolean
        get() = raw.isFile

    val isHidden: Boolean
        get() = raw.isHidden

    fun lastModified(): Long = raw.lastModified()

    fun length(): Long = raw.length()

    @Throws(IOException::class)
    fun createNewFile(): Boolean = raw.createNewFile()

    fun delete(): Boolean = raw.delete()

    fun deleteOnExit() = raw.deleteOnExit()

    fun list(): Array<String>? = raw.list()

    fun list(filter: FilenameFilter?): Array<String>? = raw.list(filter)

    fun listFiles(): Array<File>? = raw.listFiles()?.map { File(it) }?.toTypedArray()

    fun listFiles(filter: FilenameFilter?): Array<File>? =
        raw.listFiles(filter)?.map { File(it) }?.toTypedArray()

    fun listFiles(filter: FileFilter?): Array<File>? =
        raw.listFiles(filter)?.map { File(it) }?.toTypedArray()

    fun mkdir(): Boolean = raw.mkdir()

    fun mkdirs(): Boolean = raw.mkdirs()

    fun renameTo(dest: File): Boolean = raw.renameTo(dest.raw)

    fun setLastModified(time: Long): Boolean = raw.setLastModified(time)

    fun setReadOnly(): Boolean = raw.setReadOnly()

    fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean =
        raw.setWritable(writable, ownerOnly)

    fun setWritable(writable: Boolean): Boolean = raw.setWritable(writable)

    fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean =
        raw.setReadable(readable, ownerOnly)

    fun setReadable(readable: Boolean): Boolean = raw.setReadable(readable)

    fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean =
        raw.setExecutable(executable, ownerOnly)

    fun setExecutable(executable: Boolean): Boolean = raw.setExecutable(executable)

    fun canExecute(): Boolean = raw.canExecute()

    val totalSpace: Long
        get() = raw.totalSpace

    val freeSpace: Long
        get() = raw.freeSpace

    val usableSpace: Long
        get() = raw.usableSpace

    override fun compareTo(other: File): Int = raw.compareTo(other.raw)

    override fun equals(other: Any?): Boolean {
        return if (other != null) {
            val otherStr = if(other is File) other.absolutePath else other.toString()
            absolutePath == otherStr
        }
        else false
    }

    override fun toString() = raw.toString()

    override fun hashCode() = raw.hashCode()
}


val File.extension: String
    get() = name.substringAfterLast('.', "")

/**
 * Returns [path][File.path] of this File using the invariant separator '/' to
 * separate the names in the name sequence.
 */
val File.invariantSeparatorsPath: String
    get() = if (java.io.File.separatorChar != '/') path.replace(
        java.io.File.separatorChar,
        '/'
    ) else path

/**
 * Returns file's name without an extension.
 */
val File.nameWithoutExtension: String
    get() = name.substringBeforeLast(".")


fun File.copyTo(
    target: File,
    overwrite: Boolean = false,
    bufferSize: Int = DEFAULT_BUFFER_SIZE
): File = File(raw.copyTo(target.raw, overwrite, bufferSize))

fun File.copyRecursively(
    target: File,
    overwrite: Boolean = false,
    onError: (File, IOException) -> OnErrorAction = { _, exception -> throw exception }
): Boolean = raw.copyRecursively(target.raw, overwrite, onError = { f, e -> onError(File(f), e) })

fun File.deleteRecursively(): Boolean = raw.deleteRecursively()

fun File.startsWith(other: File): Boolean = raw.startsWith(other.raw)

fun File.startsWith(other: String): Boolean = raw.startsWith(other)

fun File.endsWith(other: File): Boolean = raw.endsWith(other.raw)

fun File.endsWith(other: String): Boolean = raw.endsWith(other)