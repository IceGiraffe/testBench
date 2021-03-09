<!--
Copyright (c) 2010 Yahoo! Inc., 2012 - 2016 YCSB contributors.
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you
may not use this file except in compliance with the License. You
may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
implied. See the License for the specific language governing
permissions and limitations under the License. See accompanying
LICENSE file.
-->

YCSB
====================================
YCSB running command. 

    On Linux:
    ```sh
    bin/ycsb.sh load basic -P workloads/workloada
    bin/ycsb.sh run basic -P workloads/workloada
    ```

    On Windows:
    ```bat
    bin/ycsb.bat load basic -P workloads\workloada
    bin/ycsb.bat run basic -P workloads\workloada
    ```

  Running the `ycsb` command without any argument will print the usage. 
   
  See https://github.com/brianfrankcooper/YCSB/wiki/Running-a-Workload
  for a detailed documentation on how to run a workload.

  See https://github.com/brianfrankcooper/YCSB/wiki/Core-Properties for 
  the list of available workload properties.


Building from source
--------------------

YCSB requires the use of Maven 3; if you use Maven 2, you may see [errors
such as these](https://github.com/brianfrankcooper/YCSB/issues/406).

To build the full distribution, with all database bindings:

    mvn clean package

To build a single database binding:

    mvn -pl mongodb-binding -am clean package


# 用户手册

[TOC]

---

## 一、系统总体说明

本工作主要基于YCSB进行，我们将构建多种领域不同程序下的数据库使用场景，并根据对应场景中数据库的整体表现进行多指标的报告，如下图。

<img src="F:\Research\BigData\2020_Oct 北航重点项目结题\manual_md\image-20201102002320699.png" alt="image-20201102002320699" style="zoom: 80%;" />

通常操作分为**两个部分**：

1. **load指令**：load指令将需要的数据加载进测试使用的数据库中，并根据这部分加载过程对上述指标进行测量，在load结束后就会出现上图指标，同时测试所需要的数据文件也将进入数据库。通常对同一个领域内不同的测试程序，load指令相同，且只需要执行一次。
2. **run指令**：这部分指令中常常需要从数据库中读取或写回数据，从而表现出一个计算场景的特征，这时的程序场景通常由参数进行详细区分，参数可能写在workload文件中，也可以自行传递。



## 二、Workload内容与使用说明

### 2.0 软件栈说明

2.1节所描述的TPCWorkload在JDBC上测试，后续描述的所有Workload测试场景都在MongoDB数据库（要做其他数据库的测试时，可能需要写额外代码接入项目，如人脸识别领域需要额外扩展对图片数据插入的支持）。



### 2.1 TPCWorkload（商业领域）

TPCWorkload是商业领域的Workload，我们的测试场景在JDBC数据库。

#### Workload使用操作

1. 先安装MySQL，版本是5或以前，然后获取`mysql-connector-java-x.x.x.jar`，例如此处我们使用的是`mysql-connector-java-5.1.45.jar`。将它放在根目录的TPC文件夹中，并在编译之后放入`jdbc/target/dependency`文件夹中。

2. 编译JDBC（进行过全部编译，其实也就编译过JDBC了）：

   `mvn -pl site.ycsb:jdbc-binding -am clean package -DskipTests`

3. 创建JDBC的数据表：

   - Linux：`java –cp ./jdbc/target/jdbc-binding-0.18.0-SNAPSHOT.jar:./TPC/mysql-connector-java-5.1.45.jar site.ycsb.db.JdbcDBCreateTable -P TPC/db.properties -F TPC_DS`
   - Windows：`java -cp jdbc\target\jdbc-binding-0.18.0-SNAPSHOT.jar;TPC\mysql-connector-java-5.1.45.jar site.ycsb.db.JdbcDBCreateTable -P TPC\db.properties -F TPC_DS`

4. 为JDBC运行`workload_TPC`中的配置加载数据（运行`workload_TPC`中的load部分，`-p`部分为参数，用于连接本地MySQL数据库）：

   `bin/ycsb.sh load jdbc -P workloads/workload_TPC -p db.driver=com.mysql.jdbc.Driver -p db.url=jdbc:mysql://localhost/ycsb?useSSL=false -p db.user=debian-sys-maint -p db.passwd=IIuDSA5RqwCTOHfy -F TPC_DS`

5. 用JDBC运行`workload_TPC`（此处使用参数`queryindex`确定查询的query，也就是不同的数据负载）：

   `bin/ycsb.sh run jdbc -P workloads/workload_TPC -p db.driver=com.mysql.jdbc.Driver -p db.url=jdbc:mysql://localhost/ycsb?useSSL=false -p db.user=debian-sys-maint -p db.passwd=IIuDSA5RqwCTOHfy -p queryindex=3 -F TPC_DS`

#### 数据生成操作

测试数据是代码生成的，生成代码：（Linux环境下可用）

```bash
cd TPC_DS_DataGen/v2.8.0rc4/tools
dsdgen -DIR dataGen -SCALE 1
```

该测试代码生成的数据量较大，不适合测试使用，故进行一定量的删减。

#### 测试程序指标

本领域下共5个程序场景：
- 特定时间内商品购买量查询
- 多重连接查询
- 分地区统计退款事项及原由
- 关联挖掘
- 选择查询



### 2.2 SEWorkload（搜索引擎）

#### Workload使用操作

1. 编译MongoDB（因为没有前置操作，此步可跳过，执行load指令时会自动编译；若以前编译过，则同样无需执行此操作）：

   `mvn -pl site.ycsb:mongodb-binding -am clean package -DskipTests`

   编译在服务器上时由于mvn不同可能出现得不到依赖的情况，在本机上我们将依赖的jar包打包放置于`/home/pku/dependency`下，没有依赖时可以将整个dependency文件夹复制到项目根目录的`core/target`下

2. load部分：

   `bin/ycsb.sh load mongodb -P workloads/workload_search`

3. run部分：

   `bin/ycsb.sh run mongodb -P workloads/workload_search`

#### 参数配置

`workload_SE`独有的参数：

- `task=[PageRank/NLP]`：此参数用于改变测试场景，为PageRank算法或NLP算法；
- `path=<path>`：测试数据所在目录；
  - 当测试PageRank时，所在目录第一个文件为数据集；
  - 当测试NLP时，所在目录内所有文本为数据集。
- 数据格式：PageRank算法测试数据为图数据，NLP数据为文本数据 

#### 测试程序指标

本领域下共2个程序场景：
- PageRank算法
- NLP算法



### 2.3 MLWorkload（机器学习）

#### Workload使用操作

1. 编译MongoDB（若以前编译过，则无需执行此操作）：

   `mvn -pl site.ycsb:mongodb-binding -am clean package -DskipTests`

2. load部分：

   `bin/ycsb.sh load mongodb -P workloads/workload_ai`

3. run部分：

   `bin/ycsb.sh run mongodb -P workloads/workload_ai`

#### 参数配置

MLWorkload中参数：

- `algorithm=[bayes/knn/svm/random]`：变更测试场景，对应多种机器学习算法；
- `path`位置固定在项目根目录`/data`下（无需填写）。
- 数据格式为对应机器学习算法的数据集

#### 测试程序指标

本领域下共4个程序场景：
- 贝叶斯算法
- KNN算法
- 支持向量机算法
- 随机森林算法



### 2.4 NNWorkload（神经网络）

#### Workload使用操作

1. 编译MongoDB（若以前编译过，则无需执行此操作）：

   `mvn -pl site.ycsb:mongodb-binding -am clean package -DskipTests`

2. load部分：

   `bin/ycsb.sh load mongodb -P workloads/workload_nn`

3. run部分：

   `bin/ycsb.sh run mongodb -P workloads/workload_nn`

#### 参数配置

NNWorkload中参数：

- `algorithm=[ANN/CNN/SOMcluster]`：变更测试场景，对应多种神经网络算法；
- `path`位置固定在项目根目录`/data`下（无需填写）。
- 数据格式为对应神经网络算法的数据集

#### 测试程序指标

本领域下共3个程序场景：
- ANN分类算法
- CNN分类算法
- SOM聚类算法



### 2.5 FaceWorkload（人脸识别）

#### Workload使用操作

1. 编译MongoDB（若以前编译过，则无需执行此操作）：

   `mvn -pl site.ycsb:mongodb-binding -am clean package -DskipTests`

2. 导入opencv库，此处我们使用的是`opencv-451.jar`。统一放至在`/home/pku/dependency`文件夹中，同时也在根目录的`core/opencv`中放置备份，并需在编译之后放入`core/target/dependency`文件夹中。

3. load部分：

   `bin/ycsb.sh load mongodb -P workloads/workload_face`

4. run部分：

   `bin/ycsb.sh run mongodb -P workloads/workload_face`

#### 参数配置

FaceWorkload参数：

- `algorithm=[ALT/FIRST/EXTEND/ALT2/BAYES]`：变更测试场景，对应多种人脸识别分类；
- `path`位置固定，在项目根目录`/Img/img_align_celeba`下存放测试数据（无需填写）。
- 数据格式为jpg图片，放在`path`下即可。  

#### 测试程序指标

本领域下共5个程序场景:
- 基于几何特征的算法
- 基于模板匹配的算法
- 子空间算法
- 几何特征修改版
- 基于基础贝叶斯的算法



### 2.6 社交网络部分Workload

我们为社交网络领域对应的三个测试场景分别构筑了三个workload类，不需要使用参数调整程序，即一个workload对应一个测试程序。

三个类分别为：CollaborateWorkload、LouvainWorkload、PreferenceWorkload，对应的workload文件分别为`workloada`、`workloadb`、`workloadc`。

#### Workload使用操作

下述操作以`workloada`为例：

1. 编译MongoDB（若以前编译过，则无需执行此操作）：

   `mvn -pl site.ycsb:mongodb-binding -am clean package -DskipTests`

2. load部分：

   `bin/ycsb.sh load mongodb -P workloads/workloada`

3. run部分：

   `bin\ycsb.sh run mongodb -P workloads/workloada`

#### 参数配置

- 无需使用参数变更测试场景；
- `path`位置固定，测试数据为项目根目录下的`test_data.txt`、`facebook_combined.txt`、`Ptest_data.txt`（无需填写）。
- 数据格式为图数据

#### 测试程序指标

本领域下共3个程序场景：
- 好友推荐
- 热点搜索
- 偏好估计
