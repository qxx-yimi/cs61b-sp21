# CS61B Spring 2021 学习仓库

本仓库包含了 UC Berkeley CS61B 课程（2021年春季）的实验（Labs）和项目（Projects）代码实现。该课程主要探讨高级编程技术、数据结构以及算法设计。

## 📁 目录结构

本仓库由多个部分组成，按课程进度分为 `lab`、`proj` 两大类：

### 🚀 项目 (Projects)

* **proj0: 2048 游戏**
* 实现经典 2048 游戏的逻辑核心，包括方块合并规则和游戏状态判断。


* **proj1: 双端队列 (Deque)**
* 包含链表实现（`LinkedListDeque`）和循环数组实现（`ArrayDeque`）的线性数据结构。


* **proj2: Gitlet**
* 本仓库的核心挑战。使用 Java 实现的一个简易版 Git 版本控制系统。
* 功能包括：初始化仓库（init）、提交（commit）、日志记录（log）、检出（checkout）、分支管理（branch）等。


* **proj3: BYOW (Build Your Own World)**
* 随机伪随机地图生成引擎，涉及图形界面渲染和复杂的随机算法。



### 🧪 实验 (Labs)

* **lab1 - lab4**: 基础 Java 语法、Collatz 序列分析、单元测试（JUnit）以及调试技巧（Flik）。
* **lab6: Capers**: 简单的文件持久化练习。
* **lab7: BSTMap**: 基于二叉搜索树实现的 Map 结构。
* **lab8: MyHashMap**: 基于哈希表实现的 Map 结构。
* **lab12 - lab13**: 涉及随机地图生成与图形化游戏逻辑的基础准备。

## 🛠️ 技术栈

* **语言**: Java 11+
* **构建工具**: Maven (用于管理依赖及编译过程)
* **单元测试**: JUnit 4 (用于验证数据结构和算法的正确性)
* **版本控制原理**: 序列化 (Object Serialization)、SHA-1 哈希算法 (用于 Gitlet 项目)。

## 📖 核心项目重点：Gitlet

`Gitlet` 是本仓库中最复杂的软件工程实现，它通过以下方式管理文件系统状态：

1. **持久化**: 将提交（Commit）和文件内容（Blobs）序列化并存储在 `.gitlet` 目录下。
2. **内容寻址**: 利用 SHA-1 哈希值作为唯一标识符，确保数据的完整性。
3. **分支模型**: 维护一个指针系统（HEAD, master 等）来管理不同开发路径。

---

*注意：本仓库内容仅供学习参考，请遵循课程诚实守信（Academic Integrity）准则。*
