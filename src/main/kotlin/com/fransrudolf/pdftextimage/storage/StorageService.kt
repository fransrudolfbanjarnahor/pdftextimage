import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Path
import java.util.stream.Stream

interface StorageService {
    fun init()
    fun store(file: MultipartFile?) : String
    fun storeToImage(targetFile:String, appendImage: MultipartFile?,  text: String, xT: Float, yT: Float,xI: Float, yI: Float)
    fun loadAll(): Stream<Path>
    fun load(filename: String?): Path?
    fun loadAsResource(filename: String?): Resource?
    fun deleteAll()
}