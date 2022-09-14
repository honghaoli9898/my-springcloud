package org.txlcn.tm;
 
public class DoubleLinkedListDemo {
 
    public static void main(String[] args) {
        System.out.println("双向链表测试：");
        //先创建节点
        HeroNode2 hero1 = new HeroNode2(1, "刘备", "仁义");
        HeroNode2 hero2 = new HeroNode2(2, "关羽", "武圣");
        HeroNode2 hero3 = new HeroNode2(3, "张飞", "暴躁");
        HeroNode2 hero4 = new HeroNode2(4, "赵云", "单骑救主");
 
        //创建一个双向链表
        DoubleLinkedList doubleLinkedList = new DoubleLinkedList();
        doubleLinkedList.add(hero1);
        doubleLinkedList.add(hero2);
        doubleLinkedList.add(hero3);
        doubleLinkedList.add(hero4);
 
        //显示链表
        doubleLinkedList.showList();
 
        //修改
        HeroNode2 newHeroNode = new HeroNode2(4, "好汤圆", "hsy");
        doubleLinkedList.update(newHeroNode);
        System.out.println("修改后：");
        doubleLinkedList.showList();
 
        //删除
        doubleLinkedList.delete(3);
        System.out.println("删除后：");
        doubleLinkedList.showList();
    }
 
}
 
//创建一个双向链表
class DoubleLinkedList {
    //初始化一个头节点,不存放数据
    private final HeroNode2 head = new HeroNode2(0, "", "");
 
    //返回头节点
    public HeroNode2 getHead() {
        return head;
    }
 
    //遍历
    public void showList() {
        //判断链表是否为空
        if (head.next == null) {
            System.out.println("链表为空！");
        }
        //由于头节点不能动，因此我们需要一个辅助变量来遍历
        HeroNode2 temp = head.next;
        while (true) {
            //判断链表是否到最后
            if (temp == null) {
                break;
            }
            System.out.println(temp);
            //这时需要将temp后移，否则会陷入死循环
            temp = temp.next;
        }
    }
 
    //添加一个新的节点在双向链表中
    public void add(HeroNode2 heroNode2) {
        //思路：（不考虑编号顺序）
        //1.找到当前链表的最后节点
        //2.将最后这个节点的next域指向新的节点
        HeroNode2 temp = head;
        //遍历链表，找到最后的节点
        while (true) {
            if (temp.next == null) {
                break;
            }
            //如果没有找到最后,将temp后移
            temp = temp.next;
        }
        //必须保证退出while循环时，temp指向链表的最后,并将最后这个节点的next域指向新的节点
        //形成一个双向链表
        temp.next = heroNode2;
        heroNode2.pre = temp;
    }
 
    //修改节点
    public void update(HeroNode2 newHeroNode2) {
        //判断链表是否为空
        if (head.next == null) {
            System.out.println("链表为空！");
        }
        HeroNode2 temp = head.next;
        boolean flag = false;
        while (true) {
            if (temp == null) {
                break;
            }
            if (temp.no == newHeroNode2.no) {
                //找到
                flag = true;
                break;
            }
            temp = temp.next;
        }
        //根据flag判断是否找到需要修改的值
        if (flag) {//编号已经存在
            temp.name = newHeroNode2.name;
            temp.nickname = newHeroNode2.nickname;
        } else {//没有找到
            System.out.println("没有找到编号为：" + newHeroNode2.no + "的节点，不能修改");
        }
    }
 
    //删除节点
    public void delete(int no) {
        //判断当前链表是否为空
        if (head.next==null){
            System.out.println("链表为空，无法删除");
        }
        HeroNode2 temp = head.next;
        boolean flag = false;
        while (true) {
            if (temp == null) {
                break;
            }
            if (temp.no == no) {
                //找到了待删除节点的前一个结点temp
                flag = true;
                break;
            }
            temp = temp.next;
        }
        if (flag) {
            temp.pre.next = temp.next;
            //如果要删除的是最后一个节点，就不能执行下面这句话，否则会出现空指针异常
            if (temp.next!=null){
                temp.next.pre=temp.pre;
            }
        } else {
            System.out.println("要删除的" + no + "节点不存在");
        }
    }
}
 
class HeroNode2 {
    public int no;
    public String name;
    public String nickname;
    public HeroNode2 next;//指向下一个节点
    public HeroNode2 pre;//指向上一个节点
 
    //创建构造器
    public HeroNode2(int no, String name, String nickname) {
        this.no = no;
        this.name = name;
        this.nickname = nickname;
    }
 
    @Override
    public String toString() {
        return "HeroNode{" +
                "no=" + no +
                ", name='" + name + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
