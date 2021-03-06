package com.fransrudolf.pdftextimage.storage

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("storage")
class StorageProperties {
    /**
     * Folder location for storing files
     */
    var location = "upload-dir"
}
