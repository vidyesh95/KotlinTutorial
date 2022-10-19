package concepts.classes_and_objects.classes

/**
 * Delegation pattern : Delegation pattern means delegating the responsibilities to other objects.
 */
fun main() {
    val file: String = "Delegate.txt"
    val mediaFile = MediaFile(FileDownloader(file), FilePlayer(file))
    mediaFile.download()
    mediaFile.play()
}

interface Downloader {
    fun download()
}

interface Player {
    fun play()
}

class MediaFile(
    private val downloader: Downloader,
    private val player: Player
) : Downloader by downloader, Player by player

class FileDownloader(private val file: String) : Downloader {
    override fun download() {
        println("$file downloaded.")
    }
}

class FilePlayer(private val file: String) : Player {
    override fun play() {
        println("$file playing.")
    }
}