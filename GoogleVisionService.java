package com.personalHomepage.demo.service;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.personalHomepage.demo.domain.Receipt;
import com.personalHomepage.demo.dto.ReceiptDTO;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleVisionService {
    private final ChatGptService chatGptService;

    public GoogleVisionService(ChatGptService chatGptService) {
        this.chatGptService = chatGptService;
    }


    public String detectText(ReceiptDTO receiptDTO) throws IOException {
        // TODO(developer): Replace these variables before running the sample.
//        String filePath = "src/main/resources/static/images/image.jpg";
        String filePath = "/home/ubuntu/receiptImage/image.jpg";

        return detectText(filePath, receiptDTO);
    }

    // Detects text in the specified image.
    public String detectText(String filePath, ReceiptDTO receiptDTO) throws IOException {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.

        //경로 설정
//        try (ImageAnnotatorClient client = ImageAnnotatorClient.create(ImageAnnotatorSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream("src/main/resources/static/googleApiKey/sound-decoder-405906-1688265dee96.json")))).build())) {
          try (ImageAnnotatorClient client = ImageAnnotatorClient.create(ImageAnnotatorSettings.newBuilder().setCredentialsProvider(FixedCredentialsProvider.create(ServiceAccountCredentials.fromStream(new FileInputStream("/home/ubuntu/jsonKey/sound-decoder-405906-1688265dee96.json")))).build())) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    System.out.format("Error: %s%n", res.getError().getMessage());
                    return res.getError().getMessage();
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
//                    System.out.format("Text: %s%n", annotation.getDescription());
//                    System.out.println(annotation.getDescription());
                    //gpt 서비스로 전달
                    chatGptService.detectResult(annotation.getDescription(), receiptDTO);
                    return annotation.getDescription();
//                    System.out.format("Position : %s%n", annotation.getBoundingPoly());
                }
            }
        }
        return "error";
    }

}
