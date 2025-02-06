package com.syberry.davinci.archunit.rules.likec4.loader.impl

import com.syberry.davinci.archunit.rules.likec4.loader.LikeC4ModelLoader
import software.amazon.awssdk.core.sync.ResponseTransformer
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.util.zip.ZipInputStream

/**
 * Implementation of the LikeC4ModelLoader interface that loads architecture artifacts from an S3 bucket.
 *
 * @property s3Client the S3 client used to access architecture artifacts
 * @property s3BucketName the name of the S3 bucket
 */
class S3ArchitectureArtifactLoader(
    private val s3Client: S3Client,
    private val s3BucketName: String,
) : LikeC4ModelLoader {
    /**
     * Loads the latest architecture artifact from the specified S3 bucket.
     *
     * This method retrieves the latest.zip file from the S3 bucket, extracts its contents,
     * and returns the JSON content as a string.
     *
     * @return the JSON content of the latest architecture artifact
     * @throws RuntimeException if there is an error retrieving or reading the S3 object
     */
    override fun load(): String {
        val inputStream =
            s3Client.getObject(
                GetObjectRequest
                    .builder()
                    .bucket(s3BucketName)
                    .key("latest.zip")
                    .build(),
                ResponseTransformer.toInputStream(),
            )

        ZipInputStream(inputStream).use {
            it.nextEntry
            val json = String(it.readAllBytes())
            return json
        }
    }
}
