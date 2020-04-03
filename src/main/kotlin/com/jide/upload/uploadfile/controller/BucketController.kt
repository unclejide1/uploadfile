package com.jide.upload.uploadfile.controller

import com.jide.upload.uploadfile.services.AmazonClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/storage/")
class BucketController {
    @Autowired
    lateinit var amazonClient: AmazonClient

    @PostMapping(value = ["/uploadfile"])
    fun  uploadFile(@RequestPart("file")file: MultipartFile) : String{
        return amazonClient.uploadFile(file)
    }

    @DeleteMapping(value = ["/deletefile"])
    fun deleteFile(@RequestPart(value = "url") fileUrl:String) : String{
        return amazonClient.deleteFileFromS3Bucket(fileUrl)
    }

}