<img src="https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/logo/logo-simple.png" alt="apollo-logo" width="60%">

# Apollo - A reliable configuration management system

[![Build Status](https://travis-ci.org/ctripcorp/apollo.svg?branch=master)](https://travis-ci.org/ctripcorp/apollo)
[![GitHub release](https://img.shields.io/github/release/ctripcorp/apollo.svg)](https://github.com/ctripcorp/apollo/releases)
[![Maven Central Repo](https://img.shields.io/maven-central/v/com.ctrip.framework.apollo/apollo.svg)](https://mvnrepository.com/artifact/com.ctrip.framework.apollo/apollo-client)
[![Coverage Status](https://coveralls.io/repos/github/ctripcorp/apollo/badge.svg?branch=master)](https://coveralls.io/github/ctripcorp/apollo?branch=master)
<a href="https://scan.coverity.com/projects/ctripcorp-apollo">
  <img alt="Coverity Scan Build Status" src="https://img.shields.io/coverity/scan/8244.svg"/>
</a>
[![codecov.io](https://codecov.io/github/ctripcorp/apollo/coverage.svg?branch=master)](https://codecov.io/github/ctripcorp/apollo?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)


Apollo（阿波罗）是携程框架部门研发的分布式配置中心，能够集中化管理应用不同环境、不同集群的配置，配置修改后能够实时推送到应用端，并且具备规范的权限、流程治理等特性，适用于微服务配置管理场景。

服务端基于Spring Boot和Spring Cloud开发，打包后可以直接运行，不需要额外安装Tomcat等应用容器。

Java客户端不依赖任何框架，能够运行于所有Java运行时环境，同时对Spring/Spring Boot环境也有较好的支持。

.Net客户端不依赖任何框架，能够运行于所有.Net运行时环境。

更多产品介绍参见[Apollo配置中心介绍](https://github.com/ctripcorp/apollo/wiki/Apollo%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83%E4%BB%8B%E7%BB%8D)

本地快速部署请参见[Quick Start](https://github.com/ctripcorp/apollo/wiki/Quick-Start)

演示环境（Demo）:
- [106.12.25.204:8070](http://106.12.25.204:8070/)
- 账号/密码:apollo/admin

# Screenshots
![配置界面](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/apollo-home-screenshot.png)

# Features
* **统一管理不同环境、不同集群的配置**
  * Apollo提供了一个统一界面集中式管理不同环境（environment）、不同集群（cluster）、不同命名空间（namespace）的配置。
  * 同一份代码部署在不同的集群，可以有不同的配置，比如zk的地址等
  * 通过命名空间（namespace）可以很方便的支持多个不同应用共享同一份配置，同时还允许应用对共享的配置进行覆盖

* **配置修改实时生效（热发布）**
  * 用户在Apollo修改完配置并发布后，客户端能实时（1秒）接收到最新的配置，并通知到应用程序。

* **版本发布管理**
  * 所有的配置发布都有版本概念，从而可以方便的支持配置的回滚。

* **灰度发布**
  * 支持配置的灰度发布，比如点了发布后，只对部分应用实例生效，等观察一段时间没问题后再推给所有应用实例。

* **权限管理、发布审核、操作审计**
  * 应用和配置的管理都有完善的权限管理机制，对配置的管理还分为了编辑和发布两个环节，从而减少人为的错误。
  * 所有的操作都有审计日志，可以方便的追踪问题。

* **客户端配置信息监控**
  * 可以方便的看到配置在被哪些实例使用

* **提供Java和.Net原生客户端**
  * 提供了Java和.Net的原生客户端，方便应用集成
  * 支持Spring Placeholder, Annotation和Spring Boot的ConfigurationProperties，方便应用使用（需要Spring 3.1.1+）
  * 同时提供了Http接口，非Java和.Net应用也可以方便的使用

* **提供开放平台API**
  * Apollo自身提供了比较完善的统一配置管理界面，支持多环境、多数据中心配置管理、权限、流程治理等特性。
  * 不过Apollo出于通用性考虑，对配置的修改不会做过多限制，只要符合基本的格式就能够保存。
  * 在我们的调研中发现，对于有些使用方，它们的配置可能会有比较复杂的格式，如xml, json，需要对格式做校验。
  * 还有一些使用方如DAL，不仅有特定的格式，而且对输入的值也需要进行校验后方可保存，如检查数据库、用户名和密码是否匹配。
  * 对于这类应用，Apollo支持应用方通过开放接口在Apollo进行配置的修改和发布，并且具备完善的授权和权限控制

* **部署简单**
  * 配置中心作为基础服务，可用性要求非常高，这就要求Apollo对外部依赖尽可能地少
  * 目前唯一的外部依赖是MySQL，所以部署非常简单，只要安装好Java和MySQL就可以让Apollo跑起来
  * Apollo还提供了打包脚本，一键就可以生成所有需要的安装包，并且支持自定义运行时参数

# Usage
  1. [Apollo使用指南](https://github.com/ctripcorp/apollo/wiki/Apollo%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)
  2. [Java客户端使用指南](https://github.com/ctripcorp/apollo/wiki/Java%E5%AE%A2%E6%88%B7%E7%AB%AF%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)
  3. [.Net客户端使用指南](https://github.com/ctripcorp/apollo/wiki/.Net%E5%AE%A2%E6%88%B7%E7%AB%AF%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)
  4. [其它语言客户端接入指南](https://github.com/ctripcorp/apollo/wiki/%E5%85%B6%E5%AE%83%E8%AF%AD%E8%A8%80%E5%AE%A2%E6%88%B7%E7%AB%AF%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97)
  5. [Apollo开放平台接入指南](https://github.com/ctripcorp/apollo/wiki/Apollo%E5%BC%80%E6%94%BE%E5%B9%B3%E5%8F%B0)
  6. [Apollo使用场景和示例代码](https://github.com/ctripcorp/apollo-use-cases)

# Design
  * [Apollo配置中心设计](https://github.com/ctripcorp/apollo/wiki/Apollo%E9%85%8D%E7%BD%AE%E4%B8%AD%E5%BF%83%E8%AE%BE%E8%AE%A1)
  * [Apollo核心概念之“Namespace”](https://github.com/ctripcorp/apollo/wiki/Apollo%E6%A0%B8%E5%BF%83%E6%A6%82%E5%BF%B5%E4%B9%8B%E2%80%9CNamespace%E2%80%9D)
  * [Apollo配置中心架构剖析](https://mp.weixin.qq.com/s/-hUaQPzfsl9Lm3IqQW3VDQ)
  * [Apollo源码解析](http://www.iocoder.cn/categories/Apollo/)（据说Apollo非常适合作为初学者第一个通读源码学习的分布式中间件产品）

# Development
  * [Apollo开发指南](https://github.com/ctripcorp/apollo/wiki/Apollo%E5%BC%80%E5%8F%91%E6%8C%87%E5%8D%97)
  * Code Styles
    * [Eclipse Code Style](https://github.com/ctripcorp/apollo/blob/master/apollo-buildtools/style/eclipse-java-google-style.xml)
    * [Intellij Code Style](https://github.com/ctripcorp/apollo/blob/master/apollo-buildtools/style/intellij-java-google-style.xml)

# Deployment
  * [Quick Start](https://github.com/ctripcorp/apollo/wiki/Quick-Start)
  * [分布式部署指南](https://github.com/ctripcorp/apollo/wiki/%E5%88%86%E5%B8%83%E5%BC%8F%E9%83%A8%E7%BD%B2%E6%8C%87%E5%8D%97)

# FAQ
  * [常见问题回答](https://github.com/ctripcorp/apollo/wiki/FAQ)
  * [部署&开发遇到的常见问题](https://github.com/ctripcorp/apollo/wiki/%E9%83%A8%E7%BD%B2&%E5%BC%80%E5%8F%91%E9%81%87%E5%88%B0%E7%9A%84%E5%B8%B8%E8%A7%81%E9%97%AE%E9%A2%98)

# Presentation
  * [携程开源配置中心Apollo的设计与实现](http://www.itdks.com/dakalive/detail/3420)
    * [Slides](https://myslide.cn/slides/10168)
  * [配置中心，让微服务更『智能』](https://2018.qconshanghai.com/presentation/799)
    * [Slides](https://myslide.cn/slides/10035)

# Publication
  * [开源配置中心Apollo的设计与实现](http://www.infoq.com/cn/articles/open-source-configuration-center-apollo)

# Support
<table>
  <thead>
    <th>Apollo配置中心技术支持③群<br />群号：742035428</th>
    <th>Apollo配置中心技术支持②群<br />群号：904287263（已满）</th>
    <th>Apollo配置中心技术支持①群<br />群号：375526581（已满）</th>
  </thead>
  <tbody>
    <tr>
      <td><img src="https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/tech-support-qq-3.png" alt="tech-support-qq-3"></td>
      <td><img src="https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/tech-support-qq-2.png" alt="tech-support-qq-2"></td>
      <td><img src="https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/tech-support-qq-1.png" alt="tech-support-qq-1"></td>
    </tr>
  </tbody>
</table>

# Contribution
  * Source Code: https://github.com/ctripcorp/apollo
  * Issue Tracker: https://github.com/ctripcorp/apollo/issues

# License
The project is licensed under the [Apache 2 license](https://github.com/ctripcorp/apollo/blob/master/LICENSE).

# Known Users

> 按照登记顺序排序，更多接入公司，欢迎在[https://github.com/ctripcorp/apollo/issues/451](https://github.com/ctripcorp/apollo/issues/451)登记（仅供开源用户参考）

![携程](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ctrip.png) 
![青石证券](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/bluestone.png) 
![沙绿](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/sagreen.png) 
![航旅纵横](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/umetrip.jpg) 
![58转转](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/zhuanzhuan.png) 
![蜂助手](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/phone580.png) 
![海南航空](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/hainan-airlines.png) 
![CVTE](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/cvte.png) 
![明博教育](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mainbo.jpg) 
![麻袋理财](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/madailicai.png) 
![美行科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mxnavi.jpg) 
![首展科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/fshows.jpg) 
![易微行](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/feezu.png) 
![人才加](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/rencaijia.png) 
![凯京集团](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/keking.png) 
![乐刻运动](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/leoao.png) 
![大疆](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/dji.png) 
![快看漫画](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/kkmh.png) 
![我来贷](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/wolaidai.png) 
![虚实软件](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/xsrj.png) 
![网易严选](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/yanxuan.png) 
![视觉中国](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/sjzg.png) 
![资产360](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/zc360.png) 
![亿咖通](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ecarx.png) 
![5173](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/5173.png) 
![沪江](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/hujiang.png) 
![网易云基础服务](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/163yun.png) 
![现金巴士](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/cash-bus.png) 
![锤子科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/smartisan.png) 
![头等仓](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/toodc.png) 
![吉祥航空](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/juneyaoair.png) 
![263移动通信](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/263mobile.png) 
![投投金融](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/toutoujinrong.png) 
![每天健康](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mytijian.png) 
![麦芽金服](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/maiyabank.png) 
![蜂向科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/fengunion.png) 
![即科金融](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/geex-logo.png) 
![贝壳网](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/beike.png) 
![有赞](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/youzan.png) 
![云集汇通](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/yunjihuitong.png) 
![犀牛瀚海科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/rhinotech.png) 
![农信互联](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/nxin.png) 
![蘑菇租房](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mgzf.png) 
![狐狸金服](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/huli-logo.png) 
![漫道集团](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mandao.png) 
![怪兽充电](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/enmonster.png) 
![南瓜租房](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/nanguazufang.png) 
![石投金融](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/shitoujinrong.png) 
![土巴兔](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/tubatu.png) 
![平安银行](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/payh_logo.png) 
![新新贷](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/xinxindai.png) 
![中国华戎科技集团](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/chrtc.png) 
![涂鸦智能](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/tuya_logo.png) 
![立创商城](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/szlcsc.jpg) 
![乐赚金服](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/hairongyi.png) 
![开心汽车](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/kxqc.png) 
![乐赚金服](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ppcredit.png) 
![普元信息](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/primeton.png) 
![医帮管家](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/hoskeeper.png) 
![付啦信用卡管家](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/fula.png) 
![悠哉网](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/uzai.png) 
![梧桐诚选](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/91wutong.png) 
![拍拍贷](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ppdai.png) 
![信用飞](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/xinyongfei.png) 
![丁香园](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/dxy.png) 
![国槐科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ghtech.png) 
![亲宝宝](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/qbb.png) 
![华为视频直播](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/huawei_logo.png) 
![微播易](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/weiboyi.png) 
![欧飞](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ofpay.png) 
![迷说](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mishuo.png) 
![一下科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/yixia.png) 
![DaoCloud](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/daocloud.png) 
![汽摩交易所](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/cnvex.png) 
![好未来教育集团](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/100tal.png) 
![猎户星空](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ainirobot.png) 
![卓健科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/zhuojian.png) 
![银江股份](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/enjoyor.png) 
![途虎养车](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/tuhu.png) 
![河姆渡](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/homedo.png) 
![新网银行](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/xwbank.png) 
![中旅安信云贷](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ctspcl.png) 
![美柚](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/meiyou.png) 
![震坤行](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/zkh-logo.png) 
![万谷盛世](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/wgss.png) 
![铂涛旅行](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/plateno.png) 
![乐心](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/lifesense.png) 
![亿投传媒](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/reachmedia.png) 
![股先生](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/guxiansheng.png) 
![财学堂](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/caixuetang.png) 
![4399](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/4399.png) 
![汽车之家](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/autohome.png) 
![面包财经](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mbcaijing.png) 
![虎扑](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/hoopchina.png) 
![搜狐汽车](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/sohu-auto.png) 
![量富征信](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/liangfuzhengxin.png) 
![卖好车](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/maihaoche.png) 
![中移物联网](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/zyiot.jpg) 
![易车网](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/biauto.png) 
![一药网](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/maiyaole.png) 
![小影](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/xiaoying.png) 
![彩贝壳](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/caibeike.png) 
![YEELIGHT](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/yeelight.png) 
![积目](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/itsgmu.png) 
![极致医疗](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/acmedcare.png) 
![金汇金融](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/jinhui365.png) 
![久柏易游](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/900etrip.png) 
![小麦铺](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/24xiaomai.png) 
![搜款网](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/vvic.png) 
![米庄理财](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/mizlicai.png) 
![贝吉塔网络科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/bjt.png) 
![微盟](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/weimob.png) 
![网易卡搭](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/kada.png) 
![股书](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/kapbook.png) 
![聚贸](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/jumore.png) 
![广联达bimface](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/bimface.png) 
![环球易购](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/globalgrow.png) 
![浙江执御](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/jollychic.png) 
![二维火](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/2dfire.jpg) 
![上品](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/shopin.png) 
![浪潮集团](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/inspur.png) 
![纳里健康](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/ngarihealth.png) 
![橙红科技](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/oraro.png) 
![龙腾出行](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/dragonpass.png) 
![荔枝](https://raw.githubusercontent.com/ctripcorp/apollo/master/doc/images/known-users/lizhi.fm.png) 

# Awards

<img src="https://raw.githubusercontent.com/ctripcorp/apollo-community/master/images/awards/oschina-2018-award.jpg" width="240px" alt="2018 年度最受欢迎中国开源软件">
