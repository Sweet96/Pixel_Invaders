package com.example.sweet.Pixel_Invaders.Game_Objects.PixelGroup_System;

import com.example.sweet.Pixel_Invaders.Util.Universal_Data.Constants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import static android.opengl.GLES20.GL_STREAM_DRAW;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glBufferSubData;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Sweet on 2/14/2018.
 */

public class  PixelGroup extends Collidable
{
    private static final int
            POSITION_COMPONENT_COUNT = 4,
            COLOR_COMPONENT_COUNT = 4;

    private int
            aPositionLocation,
            aColorLocation,
            aAlphaLocation,
            uSquareLengthLocation,
            uPosData,
            uTiltAngleLocation,
            uMagLoc,
            pointSizeLoc;

    private FloatBuffer alphaBuf;

    private int[]
            vBuffer = new int[1],
            aBuffer = new int[1];

    private int shaderLocation;

    private boolean glInit = false;

    public PixelGroup(Pixel[] p, float halfSquareLength, Zone[] z, CollidableGroup[] g, int sL, int vB, PixelInfo[][] iM)
    {
        super(halfSquareLength, p,true, z, g, iM);
        shaderLocation = sL;
        vBuffer[0] = vB;
    }

    public PixelGroup(Pixel[] p, float halfSquareLength, Zone[] z, int sL, int vB, PixelInfo[][] iM, Pixel[][] pM)
    {
        super(halfSquareLength, p,true, z, iM, pM);
        shaderLocation = sL;
        vBuffer[0] = vB;
    }

    public PixelGroup(Pixel[] p, float halfSquareLength, Zone[] z, int sL, int vB, PixelInfo[][] iM)
    {
        super(halfSquareLength, p,true, z, iM);
        shaderLocation = sL;
        vBuffer[0] = vB;
    }

    public void draw()
    {
        if(!glInit)
        {
            initGlCalls();
        }

        if(needsUpdate)
        {
            updatePixels();
            needsUpdate = false;
        }

        if(enableLocationChain)
        {
            while(locationDrawTail.nextLocation != null && locationDrawTail.nextLocation.collisionDone)
            {
                locationDrawTail = locationDrawTail.nextLocation;
            }
            glUniform4f(uPosData, locationDrawTail.x, locationDrawTail.y, cosA, sinA);
        }
        else
        {
            glUniform4f(uPosData, centerX, centerY, cosA, sinA);
        }

        glUniform1f(uSquareLengthLocation, halfSquareLength);

        bindAttribs();

        glDrawArrays(GL_POINTS, 0, pixels.length);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void bindAttribs()
    {
        glBindBuffer(GL_ARRAY_BUFFER, vBuffer[0]);
        glEnableVertexAttribArray(aPositionLocation);
        glVertexAttribPointer(
                aPositionLocation,
                POSITION_COMPONENT_COUNT,
                GL_FLOAT,
                false,
                Constants.BYTES_PER_FLOAT * COLOR_COMPONENT_COUNT*2,
                0
        );

        glEnableVertexAttribArray(aColorLocation);
        glVertexAttribPointer(
                aColorLocation,
                COLOR_COMPONENT_COUNT,
                GL_FLOAT,
                false,
                Constants.BYTES_PER_FLOAT * POSITION_COMPONENT_COUNT*2,
                Constants.BYTES_PER_FLOAT * POSITION_COMPONENT_COUNT
        );

        glBindBuffer(GL_ARRAY_BUFFER, aBuffer[0]);
        glEnableVertexAttribArray(aAlphaLocation);
        glVertexAttribPointer(
                aAlphaLocation,
                1,
                GL_FLOAT,
                false,
                0,
                0
        );
    }

    public void softDraw(float x, float y, float cosA, float sinA, float tiltAng)
    {
        if(!glInit)
        {
            initGlCalls();
        }

        glUniform4f(uPosData, x, y, cosA, sinA);
        glUniform1f(uSquareLengthLocation, halfSquareLength);
        glUniform1f(uTiltAngleLocation, tiltAng);

        bindAttribs();

        glDrawArrays(GL_POINTS, 0, pixels.length);

        glUniform1f(uTiltAngleLocation, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void softDraw(float x, float y, float cosA, float sinA, float tiltAng, float mag, float pointSize)
    {
        if(!glInit)
        {
            initGlCalls();
        }

        glUniform4f(uPosData, x, y, cosA, sinA);
        glUniform1f(uSquareLengthLocation, halfSquareLength*mag);
        glUniform1f(uTiltAngleLocation, tiltAng);
        glUniform1f(uMagLoc, mag);
        glUniform1f(pointSizeLoc, pointSize);

        bindAttribs();

        glDrawArrays(GL_POINTS, 0, pixels.length);

        glUniform1f(uTiltAngleLocation, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void initBuffers()
    {
        alphaBuf = FloatBuffer.allocate(pixels.length);
        alphaBuf.position(0);
        int tSize = pixels.length;
        for (int i = 0; i < tSize; i++)
        {
            if (pixels[i].state >= 1)
            {
                alphaBuf.put(i, infoMap[pixels[i].row][pixels[i].col].a);
            }
            else
            {
                alphaBuf.put(i, 0f);
            }
        }
        alphaBuf.position(0);

        glGenBuffers(1, aBuffer, 0);

        glBindBuffer(GL_ARRAY_BUFFER, aBuffer[0]);
        glBufferData(GL_ARRAY_BUFFER, alphaBuf.capacity() * Constants.BYTES_PER_FLOAT, alphaBuf, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void updatePixels()
    {
        glBindBuffer(GL_ARRAY_BUFFER, aBuffer[0]);
        glBufferSubData(GL_ARRAY_BUFFER,0,alphaBuf.capacity()*Constants.BYTES_PER_FLOAT, alphaBuf);
        //System.out.println(dif1/1000 + "   :   " + (System.nanoTime() - start)/1000);
    }
    
    public void updatePixel(Pixel p)
    {
        int i = infoMap[p.row][p.col].index;
        if(alphaBuf != null)
        {
            if(p.state == 0)
            {
                alphaBuf.put(i,0);
            }
            else if (p.state < 3)
            {
                alphaBuf.put(i,1);
            }
            else
            {
                alphaBuf.put(i,.01f);
            }
        }
    }

    public void resetPixels()
    {
        for(Pixel p: pixels)
        {
            p.state = 1;
            p.health = health;
            p.groupFlag = -1;
            if (pMap[p.row + 1][p.col] == null)
            {
                p.state = 2;
            }
            else if (pMap[p.row - 1][p.col] == null)
            {
                p.state = 2;
            }
            else if (pMap[p.row][p.col + 1] == null)
            {
                p.state = 2;
            }
            else if (pMap[p.row][p.col - 1] == null)
            {
                p.state = 2;
            }
            updatePixel(p);
        }
        collidableLive = true;
        needsUpdate = true;
        numLivePixels = totalPixels;
    }

    public void move(float mX, float mY)
    {
        centerX += mX;
        centerY += mY;
        if(knockable && readyToKnockback)
        {
            centerX += posKnockbackX;
            centerY += posKnockbackY;
            angle += angleKnockback;

            rotate(angle);
            posKnockbackX = 0;
            posKnockbackY = 0;
            angleKnockback = 0;
            readyToKnockback = false;
        }
        float tempCMX = 0;
        float tempCMY = 0;
        int numOutside = 0;
        boolean needsUpdateTemp = false;
        for(Zone z: zones)
        {
            if (z != null)
            {
                boolean zoneCheck = false;
                for (CollidableGroup cG: z.collidableGroups)
                {
                    if (cG != null)
                    {
                        boolean groupCheck = false;
                        for(Pixel p: cG.pixels)
                        {
                            if(p.state >= 1)
                            {
                                groupCheck = true;
                                if(p.state >= 2)
                                {
                                    p.xDisp = infoMap[p.row][p.col].xOriginal * cosA +
                                            infoMap[p.row][p.col].yOriginal * sinA;
                                    p.yDisp = infoMap[p.row][p.col].yOriginal * cosA -
                                            infoMap[p.row][p.col].xOriginal * sinA;
                                    tempCMX += infoMap[p.row][p.col].xOriginal;
                                    tempCMY += infoMap[p.row][p.col].yOriginal;
                                    numOutside++;
                                    if(p.state == 3)
                                    {
                                        int index = infoMap[p.row][p.col].index;
                                        if( alphaBuf != null && alphaBuf.get(index) != 0.01f)
                                        {
                                            alphaBuf.put(index, .01f);
                                            needsUpdateTemp = true;
                                        }
                                    }
                                }
                            }
                            else
                            {
                                int index = infoMap[p.row][p.col].index;
                                if( alphaBuf != null && alphaBuf.get(index) > 0)
                                {
                                    alphaBuf.put(index, 0f);
                                    needsUpdateTemp = true;
                                }
                            }
                        }
                        if(groupCheck)
                        {
                            zoneCheck = true;
                        }
                        cG.live = groupCheck;
                    }
                }
                z.live = zoneCheck;
            }
        }
        if(needsUpdateTemp)
        {
            needsUpdate = true;
        }

        if(numLivePixels < totalPixels * livablePercentage)
        {
            collidableLive = false;
        }
        if(numOutside > 0)
        {
            tempCMX /= numOutside;
            tempCMY /= numOutside;
            centerMassX = tempCMX;
            centerMassY = tempCMY;
        }
    }

    public void revivePixels(int resNum)
    {
        for(Pixel p: pixels)
        {
            if(p.state > 0)
            {
                if (pMap[p.row + 1][p.col] != null &&
                        pMap[p.row + 1][p.col].state == 0 &&
                        resNum > 0)
                {
                    resNum = revivePixelHelper(pMap[p.row + 1][p.col], resNum);
                }
                else if (pMap[p.row - 1][p.col] != null &&
                        pMap[p.row - 1][p.col].state == 0 &&
                        resNum > 0)
                {
                    resNum = revivePixelHelper(pMap[p.row - 1][p.col], resNum);
                }
                else if (pMap[p.row][p.col + 1] != null &&
                        pMap[p.row][p.col + 1].state == 0 &&
                        resNum > 0)
                {
                    resNum = revivePixelHelper(pMap[p.row][p.col + 1], resNum);
                }
                else if (pMap[p.row][p.col - 1] != null &&
                        pMap[p.row][p.col - 1].state == 0 &&
                        resNum > 0)
                {
                    resNum = revivePixelHelper(pMap[p.row][p.col - 1], resNum);
                }
            }
            if(resNum <= 0)
            {
                break;
            }
        }

        for(Pixel p: pixels)
        {
            if(p.state > 0)
            {
                p.state = 1;
                if (pMap[p.row + 1][p.col] == null)
                {
                    if (p.state < 3)
                    {
                        p.state = 2;
                    }
                }
                else if (pMap[p.row + 1][p.col].state == 0)
                {
                    p.state = 3;
                }

                if (pMap[p.row - 1][p.col] == null)
                {
                    if (p.state < 3)
                    {
                        p.state = 2;
                    }
                }
                else if (pMap[p.row - 1][p.col].state == 0)
                {
                    p.state = 3;
                }

                if (pMap[p.row][p.col + 1] == null)
                {
                    if (p.state < 3)
                    {
                        p.state = 2;
                    }
                }
                else if (pMap[p.row][p.col + 1].state == 0)
                {
                    p.state = 3;
                }

                if (pMap[p.row][p.col - 1] == null)
                {
                    if (p.state < 3)
                    {
                        p.state = 2;
                    }
                }
                else if (pMap[p.row][p.col - 1].state == 0)
                {
                    p.state = 3;
                }

                updatePixel(p);
            }
        }
        needsUpdate = true;
    }

    private int revivePixelHelper(Pixel p, int rN)
    {
        int resNum = rN;
        p.state = 1;
        p.health = health;
        numLivePixels++;
        resNum--;

        if (pMap[p.row + 1][p.col] != null &&
                pMap[p.row + 1][p.col].state == 0 &&
                resNum > 0)
        {
            resNum = revivePixelHelper(pMap[p.row + 1][p.col], resNum);
        }
        else if (pMap[p.row - 1][p.col] != null &&
                pMap[p.row - 1][p.col].state == 0 &&
                resNum > 0)
        {
            resNum = revivePixelHelper(pMap[p.row - 1][p.col], resNum);
        }
        else if (pMap[p.row][p.col + 1] != null &&
                pMap[p.row][p.col + 1].state == 0 &&
                resNum > 0)
        {
            resNum = revivePixelHelper(pMap[p.row][p.col + 1], resNum);
        }
        else if (pMap[p.row][p.col - 1] != null &&
                pMap[p.row][p.col - 1].state == 0 &&
                resNum > 0)
        {
            resNum = revivePixelHelper(pMap[p.row][p.col - 1], resNum);
        }

        return resNum;
    }

    private void initGlCalls()
    {
        aPositionLocation = glGetAttribLocation(shaderLocation, "a_Position");
        aColorLocation = glGetAttribLocation(shaderLocation, "a_Color");
        aAlphaLocation = glGetAttribLocation(shaderLocation, "alpha");
        uSquareLengthLocation = glGetUniformLocation(shaderLocation,"squareLength");
        uTiltAngleLocation = glGetUniformLocation(shaderLocation,"tilt");
        uMagLoc = glGetUniformLocation(shaderLocation,"mag");
        pointSizeLoc = glGetUniformLocation(shaderLocation,"pointSize");
        uPosData = glGetUniformLocation(shaderLocation,"posData");

        initBuffers();
        glInit = true;
    }

    public Pixel[] getPixels()
    {
        return pixels;
    }

    public Pixel[][] getpMap()
    {
        return pMap;
    }

    public void setNeedsUpdate (boolean b)
    {
        needsUpdate = b;
    }

    public void setEnableOrphanChunkDeletion(boolean b)
    {
        enableOrphanChunkDeletion = b;
    }

    public void freeMemory()
    {
        glDeleteBuffers(1, aBuffer, 0);
    }

    public PixelGroup clone()
    {
        Pixel[] pArr = new Pixel[pixels.length];
        Pixel[][] cloneMap = new Pixel[pMap.length][pMap[0].length];

        Zone[] tempZones = new Zone[zones.length];
        for(int i = 0; i < zones.length; i++)
        {
            tempZones[i] = zones[i].clone();
        }

        for(Zone z: tempZones)
        {
            for(CollidableGroup cG: z.collidableGroups)
            {
                for(Pixel p: cG.pixels)
                {
                    cloneMap[p.row][p.col] = p;
                    pArr[infoMap[p.row][p.col].index] = p;
                    p.state = infoMap[p.row][p.col].originalState;
                }
            }
        }

        PixelGroup te = new PixelGroup(
                pArr,
                halfSquareLength,
                tempZones,
                shaderLocation,
                vBuffer[0],
                infoMap,
                cloneMap
        );
        te.restorable = restorable;
        te.health = health;
        return te;
    }
}
