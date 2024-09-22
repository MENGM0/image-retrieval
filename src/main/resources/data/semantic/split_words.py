import json
import jieba
import time

def stop_words_list():
    stop_words = [line.strip() for line in open('stop_words.txt', encoding='UTF-8').readlines()]
    return stop_words

stopwords = stop_words_list()

def deal(caption_list):
    res = []
    if isinstance(caption_list, list):
        for caption in caption_list:
            tags = split_caption(caption)
            if tags != None:
                res += tags
    return list(set(res))


# 对描述分词
def split_caption(message):
    res = []
    tags = list(jieba.cut(message, cut_all=False))  # 精准模式
    # tags = list(jieba.cut(message, cut_all=False))  # 全模式
    # tags = list(jieba.cut_for_search(message))  # 搜索引擎模式
    for word in tags:
        if word not in stopwords:
            res.append(word)
    return res

def main():
    start_time = time.time()

    caption_json_path = r'F:\dataset\ai_challenger_caption_validation_20170910\caption_validation_annotations_20170910.json'
    tag_json_path = r'F:\dataset\ai_challenger_caption_validation_20170910\caption_validation_tags_20170910.json'
    f1 = open(caption_json_path, 'r', encoding='utf-8')
    f2 = open(tag_json_path, 'w+', encoding='utf-8')
    res = []

    data = json.loads(f1.read())
    for line in data:
        url = line['url']
        image_id = line['image_id']
        captions = line['caption']  # 单条数据可能有多条描述
        tags = deal(captions)
        res.append({'url': url, 'image_id': image_id, 'caption': tags})
    # 写入
    json.dump(res, f2)

    count = res.__len__()
    run_time = time.time() - start_time

    print(f"处理数据条数：{count} 条")
    print(f"程序运行时间：{run_time} 秒")
    print(f"单条处理时间：{run_time / count} 秒")


if __name__ == "__main__":
    main()