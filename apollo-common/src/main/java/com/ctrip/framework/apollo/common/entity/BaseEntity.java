package com.ctrip.framework.apollo.common.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;
import lombok.Data;

/**
 * 实体基类. <br/>
 * <p>
 * <code>@MappedSuperclass</code></p>
 * <p>@MappedSuperclass注解将实体类的多个属性分别封装到不同的非实体类中。例如，数据库表中都需要id来表示编号，id是这些映射实体类的通用的属性，交给jpa统一生成主键id编号，那么使用一个父类来封装这些通用属性，并用@MappedSuperclas标识</p>
 * <ol>
 *   <li>标注为@MappedSuperclass的类将不是一个完整的实体类，他将不会映射到数据库表，但是他的属性都将映射到其子类的数据库字段中。</li>
 *   <li>标注为@MappedSuperclass的类不能再标注@Entity或@Table注解，也无需实现序列化接口。</li>
 * </ol>
 * <p>
 * <code>@Inheritance</code></p>
 * <p>继承映射使用@Inheritance来注解，它的strategy属性的取值由枚举InheritanceType来定义（包括SINGLE_TABLE、TABLE_PER_CLASS、JOINED，分别对应三种继承策略）。@Inheritance注解只能作用于继承结构的超类上。如果不指定继承策略，默认使用SINGLE_TABLE。</p>
 * <ol>
 *   <li>一个类继承结构一个表的策略。这是继承映射的默认策略。即如果实体类B继承实体类A，实体类C也继承自实体A，那么只会映射成一个表，这个表中包括了实体类A、B、C中所有的字段，JPA使用一个叫做“discriminator列”来区分某一行数据是应该映射成哪个实体。注解为：@Inheritance(strategy = InheritanceType.SINGLE_TABLE)</li>
 *   <li> 联合子类策略。这种情况下子类的字段被映射到各自的表中，这些字段包括父类中的字段，并执行一个join操作来实例化子类。注解为：@Inheritance(strategy = InheritanceType.JOINED)</li>
 *   <li> 每个具体的类一个表的策略。注解为：@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)。</li>
 * </ol>
 *
 * @author Jason Song(song_s@ctrip.com)
 */
@Data
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BaseEntity {

  /**
   * 主键id(主键由数据库自动生成<主要是自动增长型>)
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id")
  private long id;
  /**
   * 是否删除（1: 删除, 0: 正常）
   */
  @Column(name = "IsDeleted", columnDefinition = "Bit default '0'")
  protected boolean isDeleted = false;
  /**
   * 创建者
   */
  @Column(name = "DataChange_CreatedBy", nullable = false)
  private String dataChangeCreatedBy;
  /**
   * 创建时间
   */
  @Column(name = "DataChange_CreatedTime", nullable = false)
  private Date dataChangeCreatedTime;
  /**
   * 最后修改者
   */
  @Column(name = "DataChange_LastModifiedBy")
  private String dataChangeLastModifiedBy;
  /**
   * 最后修改时间
   */
  @Column(name = "DataChange_LastTime")
  private Date dataChangeLastModifiedTime;

  /**
   * 生成最后修改时间和创建时间
   * <p>@PrePersist 可帮助我们在持久化之前自动填充实体属性。</p>
   */
  @PrePersist
  protected void prePersist() {
    if (this.dataChangeCreatedTime == null) {
      dataChangeCreatedTime = new Date();
    }
    if (this.dataChangeLastModifiedTime == null) {
      dataChangeLastModifiedTime = new Date();
    }
  }

  /**
   * 更新时更新最后修改时间，更新之前触发
   * <p> @PreUpdate  事件在实体的状态同步到数据库之前触发，此时的数据还没有真实更新到数据库。
   */
  @PreUpdate
  protected void preUpdate() {
    this.dataChangeLastModifiedTime = new Date();
  }

  /**
   * 删除时更新最后修改时间
   * <p>@PreRemove事件在实体从数据库删除之前触发，即调用了 EntityManager.remove()方法或者级联删除</p>
   */
  @PreRemove
  protected void preRemove() {
    this.dataChangeLastModifiedTime = new Date();
  }
}
