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

    public void setVec(float _vx, float _vy) {
        vx = _vx;
        vy = _vy;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float _x) {
        x = _x;
    }

    public void setY(float _y) {
        y = _y;
    }

    public void inverseVx() {
        vx = -vx;
    }

    public void inverseVy() {
        vy = -vy;
    }

    public float getRadius() {
        return radius;
    }

    public boolean isInside(float _x, float _y) {
        if (Math.pow(x - _x, 2) + Math.pow(y - _y, 2) < Math.pow(radius, 2)) {
            return true;
        } else {
            return false;
        }
    }

    public void setCoodinate(float _x, float _y) {
        x = _x;
        y = _y;
    }

    protected class Vec {
        private float x, y;

        Vec(float _x, float _y) {
            x = _x;
            y = _y;
        }

        public void add(Vec v) {
            x += v.x;
            y += v.y;
        }

        public void sub(Vec v) {
            x -= v.x;
            y -= v.y;

        }

        public void normalize() {
            float scr = (float) Math.sqrt((double) Math.pow((double) x, 2)
                    + (double) Math.pow((double) y, 2));
            x /= scr;
            y /= scr;
        }

    }

}
