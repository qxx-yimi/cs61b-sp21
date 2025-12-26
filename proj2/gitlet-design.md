# Gitlet 设计文档

## 1. 项目概述

Gitlet 是 Git 版本控制系统的简化实现，使用 Java 编写。项目实现了以下功能：

- **基础命令**: init, add, commit, rm, checkout, log, global-log, find, status
- **分支命令**: branch, rm-branch, reset, merge
- **远程命令**: add-remote, rm-remote, push, fetch, pull

## 2. 系统架构

### 2.1 核心类

```
gitlet/
├── Main.java        # 命令入口，解析参数并分派到 Repository
├── Repository.java  # 核心实现类，包含所有命令的业务逻辑
├── Commit.java      # 提交对象，表示一次提交
└── Utils.java       # 工具类（项目提供）
```

### 2.2 设计模式

- **命令模式**: Main 类解析命令，分派给 Repository 的静态方法
- **序列化存储**: 使用 Java 序列化将 Commit 对象持久化到磁盘
- **内容寻址**: 使用 SHA-1 哈希作为对象唯一标识符

## 3. 数据结构设计

### 3.1 Commit 类

```java
public class Commit implements Serializable {
    private String message;                      // 提交消息
    private String timestamp;                    // 格式化时间戳
    private String parent;                       // 第一父提交哈希
    private String secondParent;                 // 第二父提交哈希（合并用）
    private HashMap<String, String> fileSnapshots; // 文件名 -> blob 哈希映射
}
```

- **fileSnapshots**: 使用 HashMap 存储文件名到 blob 哈希的映射，避免存储冗余文件内容
- **parent/secondParent**: 使用 SHA-1 字符串而非对象引用，避免序列化时写入整个提交图

### 3.2 关键数据结构

- **暂存区 (Staging Area)**: 两个目录
  - `staging/add/`: 暂存待添加的文件，文件名为目标文件名，内容为 blob 哈希
  - `staging/remove/`: 暂存待删除的文件，文件名为目标文件名，内容为空
- **分支指针**: 存储在 `refs/heads/<分支名>` 文件中，内容为提交哈希
- **HEAD 指针**: 存储在 `HEAD` 文件中，内容为当前分支名

## 4. 目录结构

```
.gitlet/
├── HEAD                     # 当前分支名
├── refs/
│   └── heads/              # 本地分支和远程分支指针
│       ├── master           # 本地分支指针
│       ├── <其他分支>       # 本地分支指针
│       └── <远程名>/        # 远程分支指针目录
│           └── <分支名>     # 远程分支指针文件
├── remotes/                # 远程仓库配置
│   └── <远程名>            # 文件内容为远程 .gitlet 路径
├── commits/                # 提交对象存储（序列化文件）
│   └── <40位哈希>
├── objects/                # blob 对象存储
│   └── <40位哈希>          # 文件内容为原始文件内容
└── staging/
    ├── add/                # 暂存添加
    └── remove/             # 暂存删除
```

## 5. 主要命令实现

### 5.1 init

1. 创建 `.gitlet/` 目录结构
2. 创建初始提交（时间戳为 epoch 0，空文件快照）
3. 创建 master 分支指向初始提交
4. HEAD 指向 master

**关键代码**:
```java
Commit initialCommit = new Commit("initial commit", new Date(0), null, null, new HashMap<>());
String hashValue = sha1(serialize(initialCommit));
writeObject(join(COMMITS_DIR, hashValue), initialCommit);
writeContents(join(HEADS_DIR, "master"), hashValue);
writeContents(HEAD_FILE, "master");
```

### 5.2 add

1. 读取文件内容，计算 SHA-1 哈希
2. 将 blob 内容写入 `objects/` 目录
3. 在 `staging/add/` 创建暂存文件
4. 如果文件内容与当前提交相同，移除暂存
5. 如果文件之前被暂存删除，移除暂存删除标记

**优化**: 通过比较哈希值避免暂存未修改的文件。

### 5.3 commit

1. 验证暂存区非空、消息非空
2. 从当前提交复制 fileSnapshots
3. 应用暂存添加：更新 fileSnapshots 中对应文件的 blob 哈希
4. 应用暂存删除：从 fileSnapshots 移除文件
5. 清空暂存区
6. 创建新提交，更新当前分支指针

**合并提交**: 支持第二父提交，用于 merge 命令。

### 5.4 checkout

三种用法:

1. **checkout -- [文件名]**: 从 HEAD 提交检出文件
2. **checkout [commitId] -- [文件名]**: 从指定提交检出文件
   - 使用 `getFullCommitHash()` 支持部分哈希前缀
3. **checkout [branchName]**: 切换分支
   - 检查未跟踪文件冲突
   - 删除当前分支跟踪的文件
   - 写入目标分支的所有文件
   - 清空暂存区
   - 更新 HEAD

### 5.5 merge

1. 查找分叉点 (split point): 使用 BFS 找到两个分支的最近共同祖先
2. 处理文件:
   - 在分叉点后仅在目标分支修改的文件 → 检出并暂存
   - 在分叉点后仅在当前分支修改的文件 → 保持不变
   - 在分叉点后双方都修改且内容不同 → 冲突处理
   - 仅在目标分支存在的新文件 → 检出并暂存
   - 在分叉点存在但在目标分支删除的文件 → 删除
3. 创建合并提交，记录两个父提交

**分叉点查找算法**:
```java
private static String findSplitPoint(String currentCommitHash, String branchCommitHash) {
    HashSet<String> branchCommitAncestors = getAllAncestors(branchCommitHash, GITLET_DIR);
    // BFS 遍历当前分支的祖先，第一个在 branchCommitAncestors 中的就是分叉点
}
```

### 5.6 status

1. **Branches**: 列出 `refs/heads/` 下所有分支，当前分支标记 `*`
2. **Staged Files**: 列出 `staging/add/` 下所有文件
3. **Removed Files**: 列出 `staging/remove/` 下所有文件
4. **Modifications Not Staged For Commit**: 检测已修改但未暂存的文件
5. **Untracked Files**: 检测工作目录中既未跟踪也未暂存的文件

**修改检测逻辑**:
- 比较工作目录文件哈希与当前提交的哈希
- 检查暂存文件是否与工作目录一致

### 5.7 远程命令

**add-remote**: 在 `remotes/<remoteName>` 存储远程路径，创建远程分支目录

**push**:
1. 获取当前分支的所有祖先提交
2. 如果远程分支存在，检查远程提交是否是本地祖先（快速前移检查）
3. 复制所有本地 commits 和 blobs 到远程仓库
4. 更新远程分支指针

**fetch**:
1. 验证远程仓库和分支存在
2. 获取远程分支的所有祖先提交
3. 复制远程 commits 和 blobs 到本地
4. 在 `refs/heads/<远程名>/<分支名>` 创建远程分支指针

**pull**: 等价于 fetch + merge

## 6. 关键算法

### 6.1 SHA-1 内容寻址

- Blob 哈希: `sha1(文件内容)`
- 提交哈希: `sha1(序列化(commit对象))`

### 6.2 部分哈希匹配

```java
private static String getFullCommitHash(String partialHash) {
    for (String commitFileName : plainFilenamesIn(COMMITS_DIR)) {
        if (commitFileName.startsWith(partialHash)) {
            return commitFileName;
        }
    }
    return partialHash;
}
```

### 6.3 未跟踪文件冲突检测

```java
private static boolean hasUntrackedAndOverrideFiles(Commit currentCommit, Commit branchCommit) {
    for (String fileInCWD : plainFilenamesIn(CWD)) {
        boolean isTrackedInCurrentCommit = currentCommit.getFileSnapshots().containsKey(fileInCWD);
        boolean isOverWrittenByBranch = branchCommit.getFileSnapshots().containsKey(fileInCWD);
        if (!isTrackedInCurrentCommit && isOverWrittenByBranch) {
            return true;
        }
    }
    return false;
}
```

## 7. 实现特点

1. **空间优化**: 使用内容寻址存储，相同内容的文件只存储一份
2. **时间复杂度**:
   - O(1) 分支操作、暂存操作
   - O(N) 日志操作，其中 N 为提交历史长度
   - O(N + M) 合并操作，其中 N 为祖先提交数，M 为涉及的文件数量
3. **错误处理**: 使用 `GitletException` 统一处理错误，打印指定错误消息
4. **模块化**: 将复杂逻辑（如合并）拆分为独立方法

## 8. 序列化说明

使用 Java 序列化 (`Serializable`) 存储 Commit 对象：
- 优点: 自动处理对象到字节的转换
- 注意事项: 避免在 Commit 中存储对其他 Commit 的引用，否则会序列化整个提交图

解决方案: 使用 SHA-1 字符串作为引用，运行时维护映射（但不需要持久化）。
