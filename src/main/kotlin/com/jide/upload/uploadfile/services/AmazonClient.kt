package com.jide.upload.uploadfile.services



import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.util.*
import javax.annotation.PostConstruct

@Service
@PropertySource("classpath:application.yml")
class AmazonClient(

        @Value("\${amazonProperties.endpointUrl}")
        val endPointUrl: String,
        @Value("\${amazonProperties.bucketName}")
        val  bucketName: String,

        @Value("\${amazonProperties.accessKey}")
        val accessKey: String,

        @Value("\${amazonProperties.secretKey}")
        val secretKey: String

) {

        lateinit var s3Client: AmazonS3

        @PostConstruct
        fun initializeAmazon(){
                val credentials: AWSCredentials = BasicAWSCredentials(accessKey, secretKey)
                s3Client = AmazonS3ClientBuilder.standard()
                        .withCredentials(AWSStaticCredentialsProvider(credentials)).withRegion("eu-west-2")
                        .build();
        }

        private fun convertMultiPartToFile(file:MultipartFile) :File {
                val convertFile:File = File(file.originalFilename)
                val fos: FileOutputStream = FileOutputStream(convertFile)
                fos.write(file.bytes)
                fos.close()
                return convertFile

        }

        private fun generateFileName(multipartFile: MultipartFile) :String?{
                return  UUID.randomUUID().toString()+LocalDateTime.now().toString() + multipartFile.originalFilename?.replace(" ", "_")
        }

        private fun uploadFileToS3BucketPrivate(fileName:String?, file:File){
                s3Client.putObject(PutObjectRequest(bucketName,fileName,file).withCannedAcl(CannedAccessControlList.Private))
        }
        private fun uploadFileToS3BucketPublic(fileName:String?, file:File){
                s3Client.putObject(PutObjectRequest(bucketName,fileName,file).withCannedAcl(CannedAccessControlList.PublicRead))
        }

        fun uploadFile(multipartFile: MultipartFile): String{
                var fileUrl:String = ""
                try {
                    var file: File = convertMultiPartToFile(multipartFile)
                        var fileName: String? = generateFileName(multipartFile)
                        fileUrl = "$endPointUrl/$bucketName/$fileName"
                        if (fileName != null) {
                                if(fileName.contains("pdf")) {
                                        uploadFileToS3BucketPrivate(fileName, file)
                                }else{

                                        uploadFileToS3BucketPublic(fileName,file)
                                }
                        }
                        file.delete()
                }catch (e:Exception){
                        println(e.message)
                }
                return fileUrl
        }

        fun deleteFileFromS3Bucket(fileUrl: String): String{
                var fileName: String = fileUrl.substring(fileUrl.lastIndexOf("/")+1)
                s3Client.deleteObject(DeleteObjectRequest("$bucketName/", fileName))
                return "Successfully deleted"
        }





}