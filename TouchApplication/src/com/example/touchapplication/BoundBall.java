package com.example.touchapplication;

public class BoundBall {

    private float x, y, vx, vy;
    private float radius;

    public BoundBall(float _x, float _y) {
        x = _x;
        y = _y;
        radius = 10;
    }

    public BoundBall(float _x, float _y, float _r) {
        this(_x, _y);
        radius = _r;
    }

    public void run() {
        x += vx;
        y += vy;
    }

    public void addVec(float _vx, float _vy) {
        vx = _vx;
        vy = _vy;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getRadius() {
        return radius;
    }

}
