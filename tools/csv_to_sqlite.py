#!/usr/bin/env python3
import csv
import sqlite3
import sys
import time

GROUP_DESCRIPTIONS = {
    "zk": "适用于需要中考的同学",
    "gk": "适用于需要高考的同学",
    "cet4": "适用于需要考四级的同学",
    "cet6": "适用于需要考六级的同学",
    "ky": "适用于需要考研的同学",
    "toefl": "适用于需要考托福的同学",
    "gre": "适用于需要考GRE的同学",
    "ielts": "适用于需要考雅思的同学",
}

GROUP_NAME = {
    "zk": "中考词汇",
    "gk": "高考词汇",
    "cet4": "四级词汇",
    "cet6": "六级词汇",
    "ky": "考研词汇",
    "toefl": "托福词汇",
    "gre": "美国高考词汇",
    "ielts": "雅思词汇",
}

def create_tables(conn):
    cursor = conn.cursor()
    # 覆盖删除旧表
    cursor.executescript('''
        DROP TABLE IF EXISTS word_group;
        DROP TABLE IF EXISTS word_extract;
        DROP TABLE IF EXISTS words;
        DROP TABLE IF EXISTS groups;
    ''')

    # groups 表：create_time 非空（long 类型），name 可为空（String）
    cursor.execute('''
        CREATE TABLE groups (
            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            description TEXT,
            create_time INTEGER NOT NULL,
            is_learning INTEGER NOT NULL DEFAULT 0
        )
    ''')

    # words 表：word 可为空（String 无 @NonNull），id 非空自增，其余均可为空
    cursor.execute('''
        CREATE TABLE words (
            id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
            word TEXT NOT NULL,
            phonetic TEXT,
            definition TEXT,
            translation TEXT,
            pos TEXT,
            collins INTEGER,
            oxford INTEGER,
            tag TEXT,
            bnc INTEGER,
            frq INTEGER,
            exchange TEXT,
            detail TEXT,
            audio TEXT
        )
    ''')

    # word_group 关联表（外键约束）
    cursor.execute('''
        CREATE TABLE word_group (
            word_id INTEGER NOT NULL,
            group_id INTEGER NOT NULL,
            PRIMARY KEY (word_id, group_id),
            FOREIGN KEY (word_id) REFERENCES words(id) ON DELETE CASCADE,
            FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
        )
    ''')

    conn.commit()

def normalize_field(value, to_int=False):
    if value is None or value == '':
        return None
    if to_int:
        try:
            return int(value)
        except ValueError:
            return None
    return value

def open_csv(csv_path):
    """自动识别 CSV 编码：优先 UTF-8（ECDICT 官方格式），失败回退 GBK（仓库自带旧版 CSV）"""
    try:
        with open(csv_path, 'r', encoding='utf-8-sig') as probe:
            probe.read(4096)
        return open(csv_path, 'r', encoding='utf-8-sig')
    except UnicodeDecodeError:
        return open(csv_path, 'r', encoding='gbk')

def import_csv(csv_path, db_path):
    conn = sqlite3.connect(db_path)
    create_tables(conn)
    cursor = conn.cursor()

    seen_words = set()
    group_cache = {}   # tag_name -> group_id
    current_time_ms = int(time.time() * 1000)   # 用于 groups.create_time

    with open_csv(csv_path) as f:
        reader = csv.DictReader(f)
        for row in reader:
            word = row['word'].strip()
            if not word or word in seen_words:
                continue
            seen_words.add(word)

            # 插入 words 表
            cursor.execute('''
                INSERT INTO words (
                    word, phonetic, definition, translation, pos,
                    collins, oxford, tag, bnc, frq, exchange, detail, audio
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ''', (
                normalize_field(word),
                normalize_field(row['phonetic']),
                normalize_field(row['definition']),
                normalize_field(row['translation']),
                normalize_field(row['pos']),
                normalize_field(row['collins'], to_int=True),
                normalize_field(row['oxford'], to_int=True),
                normalize_field(row['tag']),
                normalize_field(row['bnc'], to_int=True),
                normalize_field(row['frq'], to_int=True),
                normalize_field(row['exchange']),
                normalize_field(row['detail']),
                normalize_field(row['audio'])
            ))
            word_id = cursor.lastrowid

            # 处理 tag -> group 关联
            tag_str = row.get('tag', '').strip()
            if tag_str:
                for tag_name in tag_str.split():
                    if not tag_name:
                        continue
                    if tag_name not in group_cache:
                        name = GROUP_NAME.get(tag_name)
                        description = GROUP_DESCRIPTIONS.get(tag_name)
                        cursor.execute('INSERT INTO groups (name, description, create_time) VALUES (?,?,?)',
                                       (name, description, current_time_ms))
                        group_cache[tag_name] = cursor.lastrowid
                    group_id = group_cache[tag_name]
                    cursor.execute('INSERT INTO word_group (word_id, group_id) VALUES (?, ?)',
                                   (word_id, group_id))

    conn.commit()
    conn.close()
    print(f"导入完成。单词数: {len(seen_words)}，组数: {len(group_cache)}")


if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("用法: python csv_to_sqlite.py <输入CSV文件> <输出SQLite数据库文件>")
        sys.exit(1)
    import_csv(sys.argv[1], sys.argv[2])
