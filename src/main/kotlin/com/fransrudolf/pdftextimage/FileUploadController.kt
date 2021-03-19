package com.fransrudolf.pdftextimage


import StorageService
import com.fransrudolf.pdftextimage.storage.StorageFileNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import java.io.File
import java.io.IOException
import java.util.stream.Collectors


@Controller
class FileUploadController @Autowired constructor(storageService: StorageService) {
    private val storageService: StorageService

    @GetMapping("/uploadform")
    @Throws(IOException::class)
    fun listUploadedFiles(model: Model): String {
        model.addAttribute("files", storageService.loadAll().map { path ->
            MvcUriComponentsBuilder.fromMethodName(FileUploadController::class.java,
                    "serveFile", path!!.getFileName().toString()).build().toUri().toString()
        }
                .collect(Collectors.toList()))

        return "uploadform"
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    fun serveFile(@PathVariable filename: String?): ResponseEntity<Resource> {
        val file: Resource = storageService.loadAsResource(filename)!!
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.filename + "\"").body(file)
    }

    @PostMapping("/upload")
    fun handleFileUpload(@RequestParam("file") file: MultipartFile,
                         @RequestParam("fileImage") fileImage: MultipartFile,
                         @RequestParam("textAppend") textAppend: String,
                         @RequestParam("xText") xText: String,
                         @RequestParam("yText") yText: String,
                         @RequestParam("xImage") xImage: String,
                         @RequestParam("yImage") yImage: String,
                         redirectAttributes: RedirectAttributes): String {
        val targetFile = storageService.store(file)
        val imageFile = storageService.store(fileImage)
        println("$targetFile $imageFile")
        storageService.storeToImage(targetFile, fileImage, textAppend, xText.toFloat(), yText.toFloat(),xImage.toFloat(), yImage.toFloat())
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.originalFilename + "!")
        return "redirect:/uploadform"
    }

    @ExceptionHandler(StorageFileNotFoundException::class)
    fun handleStorageFileNotFound(exc: StorageFileNotFoundException?): ResponseEntity<*> {
        return ResponseEntity.notFound().build<Any>()
    }

    init {
        this.storageService = storageService
    }
}