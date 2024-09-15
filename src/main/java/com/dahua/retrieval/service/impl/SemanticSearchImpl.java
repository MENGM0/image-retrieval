package com.dahua.retrieval.service.impl;

import com.dahua.retrieval.algorithm.CaptionProcess;
import com.dahua.retrieval.service.SemanticSearch;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dahua.retrieval.common.constant.Constant.ACC_VAL_CAPTION_ANNOTATION_FILE_PATH;
import static com.dahua.retrieval.common.constant.Constant.ACC_VAL_CAPTION_TAG_FILE_PATH;

@Service
public class SemanticSearchImpl implements SemanticSearch {

    CaptionProcess captionProcess = new CaptionProcess(ACC_VAL_CAPTION_TAG_FILE_PATH, ACC_VAL_CAPTION_ANNOTATION_FILE_PATH);

    @Override
    public List<String> getLabels() {
        return null;
    }

    @Override
    public List<String> getImageByWords(String[] keywords) {
        List<Map.Entry<String, Float>> list;
        try {
            list = captionProcess.searchSentence(keywords);
        }catch (Exception e){
            return null;
        }
        return list.stream().map(entry -> entry.getKey()).toList();
    }
}
