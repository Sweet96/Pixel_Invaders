package com.example.sweet.game20.Objects;

import android.content.Context;

import com.example.sweet.game20.R;
import com.example.sweet.game20.util.Constants;
import com.example.sweet.game20.util.ImageParser;

/**
 * Created by Sweet on 3/27/2018.
 */

public class BasicGun extends Gun
{
    public BasicGun(PixelGroup pG, ParticleSystem ps, double delay)
    {
        super(delay, pG, ps, .038f);
        spread = .04f;
    }

    /*public BasicGun(ParticleSystem ps)
    {
        super(1000, ps, .018f);
        //ImageParser.parseImage(context, R.drawable.basicbullet, R.drawable.basicbullet_light, tID, sL)
        spread = .06f;
    }*/

    @Override
    public boolean shoot(float x, float y, float angle)
    {
        if (System.currentTimeMillis() > lastShotTime + shootDelay * fireRateMod)
        {
            float arc = (numShots - 1) * .1f;

            for(int i = 0; i < numShots; i++)
            {
                float angDisp = (((float)(i + 1) / numShots) * arc) - (arc / 2);
                angDisp += (float)(Math.random() * spread - spread / 2);
                lastShotTime = System.currentTimeMillis();
                bulletPool.pop().resetBullet(x, y, angle + angDisp);
                addShotParticles(angle, x, y);
            }
            return true;
        }
        else
            return false;
    }

    @Override
    public void move()
    {
        for (Bullet b: bullets)
        {
            if(b.live)
            {
                b.move();
                for(Pixel p: b.pixelGroup.pixels)
                {
                    if (p.live && p.outside)
                    {
                        masterParticleSystem.addParticle(
                                p.xDisp  + b.pixelGroup.centerX, p.yDisp + b.pixelGroup.centerY,
                                b.angle,
                                p.r, p.g, p.b, .1f,
                                .04f, .005f, 10f
                        );
                    }
                }
            }
            if(!b.live)
            {
                bulletPool.push(b);
            }
        }
    }

    public void addShotParticles(float angle, float x, float y)
    {
        for (float t = 0; t < Constants.twoPI; t += .04)
        {
            masterParticleSystem.addParticle(
                    x, y,
                    t,
                    0f, 0f, 0f, .1f,
                    .4f, .1f, 10f
            );
        }
    }

    @Override
    public BasicGun clone()
    {
        return new BasicGun(pixelGroupTemplate.clone(), masterParticleSystem, shootDelay);
    }
}
