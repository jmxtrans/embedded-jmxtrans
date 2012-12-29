/*
 * Copyright 2008-2012 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.cocktail;

/*
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;*/

/**
 * Amazon AWS S3 storage for images.
 * 
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class AmazonS3FileStorageService {
/*

@Service


    private Map<String, String> contentTypeByFileExtension;

    private Map<String, String> defaultFileExtensionByContentType;

    
    private AmazonS3 amazonS3;

    @Value("${aws_s3_bucket_base_url}")
    private String amazonS3BucketBaseUrl;

    @Value("${aws_s3_bucket_name}")
    private String amazonS3BucketName;

    private final Random random = new Random();

    @Inject
    public AmazonS3FileStorageService(@Value("${AWS_ACCESS_KEY_ID}") String awsAccessKey, @Value("${AWS_SECRET_KEY}") String awsSecretKey) {

        amazonS3 = new AmazonS3Client(new BasicAWSCredentials(awsAccessKey, awsSecretKey));

        contentTypeByFileExtension = Maps.newHashMapWithExpectedSize(4);
        contentTypeByFileExtension.put("jpg", "image/jpeg");
        contentTypeByFileExtension.put("jpeg", "image/jpeg");
        contentTypeByFileExtension.put("png", "image/png");
        contentTypeByFileExtension.put("gif", "image/gif");

        defaultFileExtensionByContentType = Maps.newHashMapWithExpectedSize(3);
        defaultFileExtensionByContentType.put("image/jpeg", "jpg");
        defaultFileExtensionByContentType.put("image/png", "png");
        defaultFileExtensionByContentType.put("image/gif", "gif");
    }

    @Nullable
    public String findContentType(String fileName) {
        String fileExtension = Iterables.getLast(Splitter.on('.').split(fileName), null);
        fileExtension = Strings.nullToEmpty(fileExtension).toLowerCase();
        return contentTypeByFileExtension.get(fileExtension);
    }

    @Nonnull
    public String storeFile(InputStream in, ObjectMetadata objectMetadata) {
        String extension = defaultFileExtensionByContentType.get(objectMetadata.getContentType());
        String fileName = Math.abs(random.nextLong()) + "." + extension;

        amazonS3.putObject(amazonS3BucketName, fileName, in, objectMetadata);

        return amazonS3BucketBaseUrl + fileName;
    }*/
}
