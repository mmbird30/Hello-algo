---
comments: true
---

# 6.2. &nbsp; 哈希冲突

上节提到，**通常情况下哈希函数的输入空间远大于输出空间**，因此哈希冲突是不可避免的。例如，输入空间为全体整数，输出空间为数组容量大小，则必然有多个整数映射至同一数组索引。

哈希冲突会导致查询结果错误，严重影响哈希表的可用性。为解决该问题，我们可以每当遇到哈希冲突时就进行哈希表扩容，直至冲突消失为止。此方法简单粗暴且有效，但效率太低，因为哈希表扩容需要进行大量的数据搬运与哈希值计算。为了提升效率，我们换一种思路：

1. 改良哈希表数据结构，**使得哈希表可以在存在哈希冲突时正常工作**。
2. 仅在必要时，即当哈希冲突比较严重时，执行扩容操作。

哈希表的结构改良方法主要包括链式地址和开放寻址。

## 6.2.1. &nbsp; 链式地址

在原始哈希表中，每个桶仅能存储一个键值对。「链式地址 Separate Chaining」将单个元素转换为链表，将键值对作为链表节点，将所有发生冲突的键值对都存储在同一链表中。

![链式地址哈希表](hash_collision.assets/hash_table_chaining.png)

<p align="center"> Fig. 链式地址哈希表 </p>

链式地址下，哈希表的操作方法包括：

- **查询元素**：输入 `key` ，经过哈希函数得到数组索引，即可访问链表头节点，然后遍历链表并对比 `key` 以查找目标键值对。
- **添加元素**：先通过哈希函数访问链表头节点，然后将节点（即键值对）添加到链表中。
- **删除元素**：根据哈希函数的结果访问链表头部，接着遍历链表以查找目标节点，并将其删除。

该方法存在一些局限性，包括：

- **占用空间增大**，由于链表或二叉树包含节点指针，相比数组更加耗费内存空间；
- **查询效率降低**，因为需要线性遍历链表来查找对应元素；

以下给出了链式地址哈希表的简单实现，需要注意：

- 为了使得代码尽量简短，我们使用列表（动态数组）代替链表。换句话说，哈希表（数组）包含多个桶，每个桶都是一个列表。
- 以下代码实现了哈希表扩容方法。具体来看，当负载因子超过 $0.75$ 时，我们将哈希表扩容至 $2$ 倍。

=== "Java"

    ```java title="hash_map_chaining.java"
    /* 键值对 */
    class Pair {
        public int key;
        public String val;

        public Pair(int key, String val) {
            this.key = key;
            this.val = val;
        }
    }

    /* 链式地址哈希表 */
    class HashMapChaining {
        int size; // 键值对数量
        int capacity; // 哈希表容量
        double loadThres; // 触发扩容的负载因子阈值
        int extendRatio; // 扩容倍数
        List<List<Pair>> buckets; // 桶数组

        /* 构造方法 */
        public HashMapChaining() {
            size = 0;
            capacity = 4;
            loadThres = 2 / 3.0;
            extendRatio = 2;
            buckets = new ArrayList<>(capacity);
            for (int i = 0; i < capacity; i++) {
                buckets.add(new ArrayList<>());
            }
        }

        /* 哈希函数 */
        int hashFunc(int key) {
            return key % capacity;
        }

        /* 负载因子 */
        double loadFactor() {
            return (double) size / capacity;
        }

        /* 查询操作 */
        String get(int key) {
            int index = hashFunc(key);
            List<Pair> bucket = buckets.get(index);
            // 遍历桶，若找到 key 则返回对应 val
            for (Pair pair : bucket) {
                if (pair.key == key) {
                    return pair.val;
                }
            }
            // 若未找到 key 则返回 null
            return null;
        }

        /* 添加操作 */
        void put(int key, String val) {
            // 当负载因子超过阈值时，执行扩容
            if (loadFactor() > loadThres) {
                extend();
            }
            int index = hashFunc(key);
            List<Pair> bucket = buckets.get(index);
            // 遍历桶，若遇到指定 key ，则更新对应 val 并返回
            for (Pair pair : bucket) {
                if (pair.key == key) {
                    pair.val = val;
                    return;
                }
            }
            // 若无该 key ，则将键值对添加至尾部
            Pair pair = new Pair(key, val);
            bucket.add(pair);
            size++;
        }

        /* 删除操作 */
        void remove(int key) {
            int index = hashFunc(key);
            List<Pair> bucket = buckets.get(index);
            // 遍历桶，从中删除键值对
            for (Pair pair : bucket) {
                if (pair.key == key)
                    bucket.remove(pair);
            }
            size--;
        }

        /* 扩容哈希表 */
        void extend() {
            // 暂存原哈希表
            List<List<Pair>> bucketsTmp = buckets;
            // 初始化扩容后的新哈希表
            capacity *= extendRatio;
            buckets = new ArrayList<>(capacity);
            for (int i = 0; i < capacity; i++) {
                buckets.add(new ArrayList<>());
            }
            size = 0;
            // 将键值对从原哈希表搬运至新哈希表
            for (List<Pair> bucket : bucketsTmp) {
                for (Pair pair : bucket) {
                    put(pair.key, pair.val);
                }
            }
        }

        /* 打印哈希表 */
        void print() {
            for (List<Pair> bucket : buckets) {
                List<String> res = new ArrayList<>();
                for (Pair pair : bucket) {
                    res.add(pair.key + " -> " + pair.val);
                }
                System.out.println(res);
            }
        }
    }
    ```

=== "C++"

    ```cpp title="hash_map_chaining.cpp"
    /* 键值对 */
    struct Pair {
      public:
        int key;
        string val;
        Pair(int key, string val) {
            this->key = key;
            this->val = val;
        }
    };

    /* 链式地址哈希表 */
    class HashMapChaining {
      private:
        int size;                       // 键值对数量
        int capacity;                   // 哈希表容量
        double loadThres;               // 触发扩容的负载因子阈值
        int extendRatio;                // 扩容倍数
        vector<vector<Pair *>> buckets; // 桶数组

      public:
        /* 构造方法 */
        HashMapChaining() : size(0), capacity(4), loadThres(2.0 / 3), extendRatio(2) {
            buckets.resize(capacity);
        }

        /* 哈希函数 */
        int hashFunc(int key) {
            return key % capacity;
        }

        /* 负载因子 */
        double loadFactor() {
            return (double)size / (double)capacity;
        }

        /* 查询操作 */
        string get(int key) {
            int index = hashFunc(key);
            // 遍历桶，若找到 key 则返回对应 val
            for (Pair *pair : buckets[index]) {
                if (pair->key == key) {
                    return pair->val;
                }
            }
            // 若未找到 key 则返回 nullptr
            return nullptr;
        }

        /* 添加操作 */
        void put(int key, string val) {
            // 当负载因子超过阈值时，执行扩容
            if (loadFactor() > loadThres) {
                extend();
            }
            int index = hashFunc(key);
            // 遍历桶，若遇到指定 key ，则更新对应 val 并返回
            for (Pair *pair : buckets[index]) {
                if (pair->key == key) {
                    pair->val = val;
                    return;
                }
            }
            // 若无该 key ，则将键值对添加至尾部
            buckets[index].push_back(new Pair(key, val));
            size++;
        }

        /* 删除操作 */
        void remove(int key) {
            int index = hashFunc(key);
            auto &bucket = buckets[index];
            // 遍历桶，从中删除键值对
            for (int i = 0; i < bucket.size(); i++) {
                if (bucket[i]->key == key) {
                    Pair *tmp = bucket[i];
                    bucket.erase(bucket.begin() + i); // 从中删除键值对
                    delete tmp;                       // 释放内存
                    size--;
                    return;
                }
            }
        }

        /* 扩容哈希表 */
        void extend() {
            // 暂存原哈希表
            vector<vector<Pair *>> bucketsTmp = buckets;
            // 初始化扩容后的新哈希表
            capacity *= extendRatio;
            buckets.clear();
            buckets.resize(capacity);
            size = 0;
            // 将键值对从原哈希表搬运至新哈希表
            for (auto &bucket : bucketsTmp) {
                for (Pair *pair : bucket) {
                    put(pair->key, pair->val);
                }
            }
        }

        /* 打印哈希表 */
        void print() {
            for (auto &bucket : buckets) {
                cout << "[";
                for (Pair *pair : bucket) {
                    cout << pair->key << " -> " << pair->val << ", ";
                }
                cout << "]\n";
            }
        }
    };
    ```

=== "Python"

    ```python title="hash_map_chaining.py"
    class Pair:
        """键值对"""

        def __init__(self, key: int, val: str):
            self.key = key
            self.val = val

    class HashMapChaining:
        """链式地址哈希表"""

        def __init__(self):
            """构造方法"""
            self.size = 0  # 键值对数量
            self.capacity = 4  # 哈希表容量
            self.load_thres = 2 / 3  # 触发扩容的负载因子阈值
            self.extend_ratio = 2  # 扩容倍数
            self.buckets = [[] for _ in range(self.capacity)]  # 桶数组

        def hash_func(self, key: int) -> int:
            """哈希函数"""
            return key % self.capacity

        def load_factor(self) -> float:
            """负载因子"""
            return self.size / self.capacity

        def get(self, key: int) -> str:
            """查询操作"""
            index = self.hash_func(key)
            bucket = self.buckets[index]
            # 遍历桶，若找到 key 则返回对应 val
            for pair in bucket:
                if pair.key == key:
                    return pair.val
            # 若未找到 key 则返回 None
            return None

        def put(self, key: int, val: str):
            """添加操作"""
            # 当负载因子超过阈值时，执行扩容
            if self.load_factor() > self.load_thres:
                self.extend()
            index = self.hash_func(key)
            bucket = self.buckets[index]
            # 遍历桶，若遇到指定 key ，则更新对应 val 并返回
            for pair in bucket:
                if pair.key == key:
                    pair.val = val
                    return
            # 若无该 key ，则将键值对添加至尾部
            pair = Pair(key, val)
            bucket.append(pair)
            self.size += 1

        def remove(self, key: int):
            """删除操作"""
            index = self.hash_func(key)
            bucket = self.buckets[index]
            # 遍历桶，从中删除键值对
            for pair in bucket:
                if pair.key == key:
                    bucket.remove(pair)
                    self.size -= 1
                    return

        def extend(self):
            """扩容哈希表"""
            # 暂存原哈希表
            buckets = self.buckets
            # 初始化扩容后的新哈希表
            self.capacity *= self.extend_ratio
            self.buckets = [[] for _ in range(self.capacity)]
            self.size = 0
            # 将键值对从原哈希表搬运至新哈希表
            for bucket in buckets:
                for pair in bucket:
                    self.put(pair.key, pair.val)

        def print(self):
            """打印哈希表"""
            for bucket in self.buckets:
                res = []
                for pair in bucket:
                    res.append(str(pair.key) + " -> " + pair.val)
                print(res)
    ```

=== "Go"

    ```go title="hash_map_chaining.go"
    [class]{pair}-[func]{}

    [class]{hashMapChaining}-[func]{}
    ```

=== "JavaScript"

    ```javascript title="hash_map_chaining.js"
    [class]{Pair}-[func]{}

    [class]{HashMapChaining}-[func]{}
    ```

=== "TypeScript"

    ```typescript title="hash_map_chaining.ts"
    [class]{Pair}-[func]{}

    [class]{HashMapChaining}-[func]{}
    ```

=== "C"

    ```c title="hash_map_chaining.c"
    [class]{pair}-[func]{}

    [class]{hashMapChaining}-[func]{}
    ```

=== "C#"

    ```csharp title="hash_map_chaining.cs"
    [class]{Pair}-[func]{}

    [class]{HashMapChaining}-[func]{}
    ```

=== "Swift"

    ```swift title="hash_map_chaining.swift"
    [class]{Pair}-[func]{}

    [class]{HashMapChaining}-[func]{}
    ```

=== "Zig"

    ```zig title="hash_map_chaining.zig"
    [class]{Pair}-[func]{}

    [class]{HashMapChaining}-[func]{}
    ```

=== "Dart"

    ```dart title="hash_map_chaining.dart"
    [class]{Pair}-[func]{}

    [class]{HashMapChaining}-[func]{}
    ```

!!! tip

    为了提高效率，**我们可以将链表转换为「AVL 树」或「红黑树」**，从而将查询操作的时间复杂度优化至 $O(\log n)$ 。

## 6.2.2. &nbsp; 开放寻址

「开放寻址 Open Addressing」不引入额外的数据结构，而是通过“多次探测”来处理哈希冲突，探测方式主要包括线性探测、平方探测、多次哈希。

### 线性探测

线性探测采用固定步长的线性查找来进行探测，对应的哈希表操作方法为：

- **插入元素**：通过哈希函数计算数组索引，若发现桶内已有元素，则从冲突位置向后线性遍历（步长通常为 $1$ ），直至找到空位，将元素插入其中。
- **查找元素**：若发现哈希冲突，则使用相同步长向后线性遍历，直到找到对应元素，返回 `value` 即可；或者若遇到空位，说明目标键值对不在哈希表中，返回 $\text{None}$ 。

![线性探测](hash_collision.assets/hash_table_linear_probing.png)

<p align="center"> Fig. 线性探测 </p>

然而，线性探测存在以下缺陷：

- **不能直接删除元素**。删除元素会在数组内产生一个空位，查找其他元素时，该空位可能导致程序误判元素不存在。因此，需要借助一个标志位来标记已删除元素。
- **容易产生聚集**。数组内连续被占用位置越长，这些连续位置发生哈希冲突的可能性越大，进一步促使这一位置的“聚堆生长”，最终导致增删查改操作效率降低。

如以下代码所示，为开放寻址（线性探测）哈希表的简单实现，重点包括：

- 我们使用一个固定的键值对实例 `removed` 来标记已删除元素。也就是说，当一个桶为 $\text{None}$ 或 `removed` 时，这个桶都是空的，可用于放置键值对。
- 被标记为已删除的空间是可以再次被使用的。当插入元素时，若通过哈希函数找到了被标记为已删除的索引，则可将该元素放置到该索引。
- 在线性探测时，我们从当前索引 `index` 向后遍历；而当越过数组尾部时，需要回到头部继续遍历。

=== "Java"

    ```java title="hash_map_open_addressing.java"
    /* 键值对 */
    class Pair {
        public int key;
        public String val;

        public Pair(int key, String val) {
            this.key = key;
            this.val = val;
        }
    }

    /* 开放寻址哈希表 */
    class HashMapOpenAddressing {
        private int size; // 键值对数量
        private int capacity; // 哈希表容量
        private double loadThres; // 触发扩容的负载因子阈值
        private int extendRatio; // 扩容倍数
        private Pair[] buckets; // 桶数组
        private Pair removed; // 删除标记

        /* 构造方法 */
        public HashMapOpenAddressing() {
            size = 0;
            capacity = 4;
            loadThres = 2.0 / 3.0;
            extendRatio = 2;
            buckets = new Pair[capacity];
            removed = new Pair(-1, "-1");
        }

        /* 哈希函数 */
        public int hashFunc(int key) {
            return key % capacity;
        }

        /* 负载因子 */
        public double loadFactor() {
            return (double) size / capacity;
        }

        /* 查询操作 */
        public String get(int key) {
            int index = hashFunc(key);
            // 线性探测，从 index 开始向后遍历
            for (int i = 0; i < capacity; i++) {
                // 计算桶索引，越过尾部返回头部
                int j = (index + i) % capacity;
                // 若遇到空桶，说明无此 key ，则返回 null
                if (buckets[j] == null)
                    return null;
                // 若遇到指定 key ，则返回对应 val
                if (buckets[j].key == key && buckets[j] != removed)
                    return buckets[j].val;
            }
            return null;
        }

        /* 添加操作 */
        public void put(int key, String val) {
            // 当负载因子超过阈值时，执行扩容
            if (loadFactor() > loadThres) {
                extend();
            }
            int index = hashFunc(key);
            // 线性探测，从 index 开始向后遍历
            for (int i = 0; i < capacity; i++) {
                // 计算桶索引，越过尾部返回头部
                int j = (index + i) % capacity;
                // 若遇到空桶、或带有删除标记的桶，则将键值对放入该桶
                if (buckets[j] == null || buckets[j] == removed) {
                    buckets[j] = new Pair(key, val);
                    size += 1;
                    return;
                }
                // 若遇到指定 key ，则更新对应 val
                if (buckets[j].key == key) {
                    buckets[j].val = val;
                    return;
                }
            }
        }

        /* 删除操作 */
        public void remove(int key) {
            int index = hashFunc(key);
            // 线性探测，从 index 开始向后遍历
            for (int i = 0; i < capacity; i++) {
                // 计算桶索引，越过尾部返回头部
                int j = (index + i) % capacity;
                // 若遇到空桶，说明无此 key ，则直接返回
                if (buckets[j] == null) {
                    return;
                }
                // 若遇到指定 key ，则标记删除并返回
                if (buckets[j].key == key) {
                    buckets[j] = removed;
                    size -= 1;
                    return;
                }
            }
        }

        /* 扩容哈希表 */
        public void extend() {
            // 暂存原哈希表
            Pair[] bucketsTmp = buckets;
            // 初始化扩容后的新哈希表
            capacity *= extendRatio;
            buckets = new Pair[capacity];
            size = 0;
            // 将键值对从原哈希表搬运至新哈希表
            for (Pair pair : bucketsTmp) {
                if (pair != null && pair != removed) {
                    put(pair.key, pair.val);
                }
            }
        }

        /* 打印哈希表 */
        public void print() {
            for (Pair pair : buckets) {
                if (pair != null) {
                    System.out.println(pair.key + " -> " + pair.val);
                } else {
                    System.out.println("null");
                }
            }
        }
    }
    ```

=== "C++"

    ```cpp title="hash_map_open_addressing.cpp"
    /* 键值对 */
    struct Pair {
        int key;
        string val;

        Pair(int k, string v) : key(k), val(v) {
        }
    };

    /* 开放寻址哈希表 */
    class HashMapOpenAddressing {
      private:
        int size;               // 键值对数量
        int capacity;           // 哈希表容量
        double loadThres;       // 触发扩容的负载因子阈值
        int extendRatio;        // 扩容倍数
        vector<Pair *> buckets; // 桶数组
        Pair *removed;          // 删除标记

      public:
        /* 构造方法 */
        HashMapOpenAddressing() {
            // 构造方法
            size = 0;
            capacity = 4;
            loadThres = 2.0 / 3.0;
            extendRatio = 2;
            buckets = vector<Pair *>(capacity, nullptr);
            removed = new Pair(-1, "-1");
        }

        /* 哈希函数 */
        int hashFunc(int key) {
            return key % capacity;
        }

        /* 负载因子 */
        double loadFactor() {
            return static_cast<double>(size) / capacity;
        }

        /* 查询操作 */
        string get(int key) {
            int index = hashFunc(key);
            // 线性探测，从 index 开始向后遍历
            for (int i = 0; i < capacity; i++) {
                // 计算桶索引，越过尾部返回头部
                int j = (index + i) % capacity;
                // 若遇到空桶，说明无此 key ，则返回 nullptr
                if (buckets[j] == nullptr)
                    return nullptr;
                // 若遇到指定 key ，则返回对应 val
                if (buckets[j]->key == key && buckets[j] != removed)
                    return buckets[j]->val;
            }
            return nullptr;
        }

        /* 添加操作 */
        void put(int key, string val) {
            // 当负载因子超过阈值时，执行扩容
            if (loadFactor() > loadThres)
                extend();
            int index = hashFunc(key);
            // 线性探测，从 index 开始向后遍历
            for (int i = 0; i < capacity; i++) {
                // 计算桶索引，越过尾部返回头部
                int j = (index + i) % capacity;
                // 若遇到空桶、或带有删除标记的桶，则将键值对放入该桶
                if (buckets[j] == nullptr || buckets[j] == removed) {
                    buckets[j] = new Pair(key, val);
                    size += 1;
                    return;
                }
                // 若遇到指定 key ，则更新对应 val
                if (buckets[j]->key == key) {
                    buckets[j]->val = val;
                    return;
                }
            }
        }

        /* 删除操作 */
        void remove(int key) {
            int index = hashFunc(key);
            // 线性探测，从 index 开始向后遍历
            for (int i = 0; i < capacity; i++) {
                // 计算桶索引，越过尾部返回头部
                int j = (index + i) % capacity;
                // 若遇到空桶，说明无此 key ，则直接返回
                if (buckets[j] == nullptr)
                    return;
                // 若遇到指定 key ，则标记删除并返回
                if (buckets[j]->key == key) {
                    delete buckets[j]; // 释放内存
                    buckets[j] = removed;
                    size -= 1;
                    return;
                }
            }
        }

        /* 扩容哈希表 */
        void extend() {
            // 暂存原哈希表
            vector<Pair *> bucketsTmp = buckets;
            // 初始化扩容后的新哈希表
            capacity *= extendRatio;
            buckets = vector<Pair *>(capacity, nullptr);
            size = 0;
            // 将键值对从原哈希表搬运至新哈希表
            for (Pair *pair : bucketsTmp) {
                if (pair != nullptr && pair != removed) {
                    put(pair->key, pair->val);
                }
            }
        }

        /* 打印哈希表 */
        void print() {
            for (auto &pair : buckets) {
                if (pair != nullptr) {
                    cout << pair->key << " -> " << pair->val << endl;
                } else {
                    cout << "nullptr" << endl;
                }
            }
        }
    };
    ```

=== "Python"

    ```python title="hash_map_open_addressing.py"
    class Pair:
        """键值对"""

        def __init__(self, key: int, val: str):
            self.key = key
            self.val = val

    class HashMapOpenAddressing:
        """开放寻址哈希表"""

        def __init__(self):
            """构造方法"""
            self.size = 0  # 键值对数量
            self.capacity = 4  # 哈希表容量
            self.load_thres = 2 / 3  # 触发扩容的负载因子阈值
            self.extend_ratio = 2  # 扩容倍数
            self.buckets: list[Pair | None] = [None] * self.capacity  # 桶数组
            self.removed = Pair(-1, "-1")  # 删除标记

        def hash_func(self, key: int) -> int:
            """哈希函数"""
            return key % self.capacity

        def load_factor(self) -> float:
            """负载因子"""
            return self.size / self.capacity

        def get(self, key: int) -> str:
            """查询操作"""
            index = self.hash_func(key)
            # 线性探测，从 index 开始向后遍历
            for i in range(self.capacity):
                # 计算桶索引，越过尾部返回头部
                j = (index + i) % self.capacity
                # 若遇到空桶，说明无此 key ，则返回 None
                if self.buckets[j] is None:
                    return None
                # 若遇到指定 key ，则返回对应 val
                if self.buckets[j].key == key and self.buckets[j] != self.removed:
                    return self.buckets[j].val

        def put(self, key: int, val: str):
            """添加操作"""
            # 当负载因子超过阈值时，执行扩容
            if self.load_factor() > self.load_thres:
                self.extend()
            index = self.hash_func(key)
            # 线性探测，从 index 开始向后遍历
            for i in range(self.capacity):
                # 计算桶索引，越过尾部返回头部
                j = (index + i) % self.capacity
                # 若遇到空桶、或带有删除标记的桶，则将键值对放入该桶
                if self.buckets[j] in [None, self.removed]:
                    self.buckets[j] = Pair(key, val)
                    self.size += 1
                    return
                # 若遇到指定 key ，则更新对应 val
                if self.buckets[j].key == key:
                    self.buckets[j].val = val
                    return

        def remove(self, key: int):
            """删除操作"""
            index = self.hash_func(key)
            # 线性探测，从 index 开始向后遍历
            for i in range(self.capacity):
                # 计算桶索引，越过尾部返回头部
                j = (index + i) % self.capacity
                # 若遇到空桶，说明无此 key ，则直接返回
                if self.buckets[j] is None:
                    return
                # 若遇到指定 key ，则标记删除并返回
                if self.buckets[j].key == key:
                    self.buckets[j] = self.removed
                    self.size -= 1
                    return

        def extend(self):
            """扩容哈希表"""
            # 暂存原哈希表
            buckets_tmp = self.buckets
            # 初始化扩容后的新哈希表
            self.capacity *= self.extend_ratio
            self.buckets = [None] * self.capacity
            self.size = 0
            # 将键值对从原哈希表搬运至新哈希表
            for pair in buckets_tmp:
                if pair not in [None, self.removed]:
                    self.put(pair.key, pair.val)

        def print(self) -> None:
            """打印哈希表"""
            for pair in self.buckets:
                if pair is not None:
                    print(pair.key, "->", pair.val)
                else:
                    print("None")
    ```

=== "Go"

    ```go title="hash_map_open_addressing.go"
    [class]{pair}-[func]{}

    [class]{hashMapOpenAddressing}-[func]{}
    ```

=== "JavaScript"

    ```javascript title="hash_map_open_addressing.js"
    [class]{Pair}-[func]{}

    [class]{HashMapOpenAddressing}-[func]{}
    ```

=== "TypeScript"

    ```typescript title="hash_map_open_addressing.ts"
    [class]{Pair}-[func]{}

    [class]{HashMapOpenAddressing}-[func]{}
    ```

=== "C"

    ```c title="hash_map_open_addressing.c"
    [class]{pair}-[func]{}

    [class]{hashMapOpenAddressing}-[func]{}
    ```

=== "C#"

    ```csharp title="hash_map_open_addressing.cs"
    [class]{Pair}-[func]{}

    [class]{HashMapOpenAddressing}-[func]{}
    ```

=== "Swift"

    ```swift title="hash_map_open_addressing.swift"
    [class]{Pair}-[func]{}

    [class]{HashMapOpenAddressing}-[func]{}
    ```

=== "Zig"

    ```zig title="hash_map_open_addressing.zig"
    [class]{Pair}-[func]{}

    [class]{HashMapOpenAddressing}-[func]{}
    ```

=== "Dart"

    ```dart title="hash_map_open_addressing.dart"
    [class]{Pair}-[func]{}

    [class]{HashMapOpenAddressing}-[func]{}
    ```

### 多次哈希

顾名思义，多次哈希方法是使用多个哈希函数 $f_1(x)$ , $f_2(x)$ , $f_3(x)$ , $\cdots$ 进行探测。

- **插入元素**：若哈希函数 $f_1(x)$ 出现冲突，则尝试 $f_2(x)$ ，以此类推，直到找到空位后插入元素。
- **查找元素**：在相同的哈希函数顺序下进行查找，直到找到目标元素时返回；或遇到空位或已尝试所有哈希函数，说明哈希表中不存在该元素，则返回 $\text{None}$ 。

与线性探测相比，多次哈希方法不易产生聚集，但多个哈希函数会增加额外的计算量。

!!! note "编程语言的选择"

    Java 采用链式地址。自 JDK 1.8 以来，当 HashMap 内数组长度达到 64 且链表长度达到 8 时，链表会被转换为红黑树以提升查找性能。

    Python 采用开放寻址。字典 dict 使用伪随机数进行探测。

    Golang 采用链式地址。Go 规定每个桶最多存储 8 个键值对，超出容量则连接一个溢出桶；当溢出桶过多时，会执行一次特殊的等量扩容操作，以确保性能。