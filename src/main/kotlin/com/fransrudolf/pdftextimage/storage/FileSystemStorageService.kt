package com.fransrudolf.pdftextimage.storage

import StorageService
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.tools.imageio.ImageIOUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.hateoas.mediatype.alps.Alps.doc
import org.springframework.stereotype.Service
import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Stream


@Service
class FileSystemStorageService @Autowired constructor(properties: StorageProperties) : StorageService {
    private var rootLocation: Path? = null

    @Autowired
    fun FileSystemStorageService(properties: StorageProperties) {

        rootLocation = Paths.get(properties.location)
        print(rootLocation)
    }

    override fun init() {
        try {
            Files.createDirectories(rootLocation)
        } catch (e: IOException) {
            throw StorageException("Could not initialize storage", e)
        }
    }

    override fun store(file: MultipartFile?): String {
        try {
            if (file!!.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }
            val destinationFile: Path = this.rootLocation!!.resolve(
                    Paths.get(file.originalFilename))
                    .normalize().toAbsolutePath()
            println("destinationFolder " + this.rootLocation!!
                    .normalize().toAbsolutePath())
            if (destinationFile.parent != this.rootLocation!!.toAbsolutePath()) {
                // This is a security check
                throw StorageException(
                        "Cannot store file outside current directory.")
            }
            file.inputStream.use { inputStream ->
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING)

            }
            return destinationFile.toString();

            //PDFImageWriter.write(document, "png", null, 0, 0, "picture");


        } catch (e: IOException) {
            throw StorageException("Failed to store file.", e)
        }
    }

    override fun storeToImage(targetFile: String, appendImage: MultipartFile?, text: String, xT: Float, yT: Float, xI: Float, yI: Float) {
        val document: PDDocument = PDDocument.load(File(targetFile))

        val pages = document.pages

        for (p in pages) {
            val contentStream = PDPageContentStream(document, p, PDPageContentStream.AppendMode.APPEND, false)
            if (text.isNotEmpty()) {
                contentStream.beginText()
                contentStream.setFont(PDType1Font.TIMES_ROMAN, 42f)
                contentStream.newLineAtOffset(xT, yT)
                contentStream.showText(text)
                contentStream.endText()

            }
            if (appendImage != null) {
                val imageAppend: Path = this.rootLocation!!.resolve(
                        Paths.get(appendImage!!.originalFilename))
                        .normalize().toAbsolutePath()
                val pdImage = PDImageXObject.createFromFile(imageAppend.toString(), document)
                contentStream.drawImage(pdImage, xI, yI)
                contentStream.close()
            }

        }

        val pdfRenderer = PDFRenderer(document)
        val numberOfPages = document.numberOfPages - 1
        for (i in 0..numberOfPages) {
            //val bffim = pdfRenderer.renderImageWithDPI(i, 300f, ImageType.RGB)
            val image: BufferedImage = pdfRenderer.renderImageWithDPI(i, 300f, ImageType.RGB)

            //  val filename = File("/Users/ff/PROJECTS/pdftextimage/upload-dir/image1.png")

            ImageIOUtil.writeImage(image, rootLocation.toString() + "/image-" + i.toString() + ".png", 150, 0.1f)

        }
        document.close();
    }

    override fun loadAll(): Stream<Path> {
        return try {
            Files.walk(rootLocation, 1)
                    .filter { path: Path -> path != rootLocation }
                    .map { other: Path? -> rootLocation!!.relativize(other) }
        } catch (e: IOException) {
            throw StorageException("Failed to read stored files", e)
        }
    }

    override fun load(filename: String?): Path? {
        return rootLocation!!.resolve(filename)
    }

    override fun loadAsResource(filename: String?): Resource? {
        return try {
            val file = load(filename)
            val resource: Resource = UrlResource(file!!.toUri())
            if (resource.exists() || resource.isReadable) {
                resource
            } else {
                throw StorageFileNotFoundException(
                        "Could not read file: $filename")
            }
        } catch (e: MalformedURLException) {
            throw StorageFileNotFoundException("Could not read file: $filename", e)
        }
    }

    override fun deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation!!.toFile())
    }

}