package com.example.product;

public class ProductVO {
    private int code;
    private String name;
    private String image;
    public int price;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ProductVO{" +
                "code=" + code +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", price=" + price +
                '}';
    }
}
