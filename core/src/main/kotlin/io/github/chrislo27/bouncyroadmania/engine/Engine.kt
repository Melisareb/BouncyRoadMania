package io.github.chrislo27.bouncyroadmania.engine

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.bouncyroadmania.engine.clock.Clock
import io.github.chrislo27.bouncyroadmania.engine.input.InputType
import io.github.chrislo27.bouncyroadmania.renderer.PaperProjection
import io.github.chrislo27.toolboks.registry.AssetRegistry


class Engine(val clock: Clock) {

    companion object {
        private val TMP_MATRIX = Matrix4()

        val MAX_OFFSET_SEC: Float = 4.5f / 60
        val ACE_OFFSET: Float = 1f / 60
        val GOOD_OFFSET: Float = 3f / 60
        val BARELY_OFFSET: Float = 4f / 60
    }

    val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    val entities: MutableList<Entity> = mutableListOf()
    val projector = PaperProjection(2f)
    var bouncers: List<Bouncer> = listOf()
    lateinit var yellowBouncer: YellowBouncer
        private set
    lateinit var redBouncer: RedBouncer
        private set

    fun addBouncers() {
        entities.removeAll(bouncers)
        bouncers = listOf()

        val radius = 1200f
        val bouncers = mutableListOf<Bouncer>()
        // midpoint index = 8
        for (i in -1..15) {
            val angle = (180.0 * (i / 14.0)) - 90
            val bouncer: Bouncer = if (i == 13) {
                RedBouncer(this).apply {
                    redBouncer = this
                }
            } else if (i == 12) {
                YellowBouncer(this).apply {
                    yellowBouncer = this
                }
            } else {
                Bouncer(this)
            }

            if (i !in 0 until 15) {
                bouncer.isSilent = true
            } else if (i == 14) {
                bouncer.soundHandle = "sfx_cymbal"
            }

            val sin = Math.sin(Math.toRadians(angle)).toFloat()
            val z: Float = (-sin + 1) * 0.3f

            bouncer.posY = Interpolation.sineIn.apply(640f, 150f, i / 14f)

            bouncer.posX = radius * if (i <= 8) Interpolation.sineOut.apply(i / 8f) else Interpolation.sineOut.apply(1f - ((i - 8) / 6f))
            bouncer.posZ = z

            bouncers += bouncer
        }
//        bouncers += Bouncer(this).apply {
//            isSilent = true
//            posX = -50f
//            posY = 150f
//            posZ = 0f
//        }
//        bouncers += Bouncer(this).apply {
//            isSilent = true
//            posX = -50f
//            posY = 150f
//            posZ = 0.25f
//        }
//        bouncers += Bouncer(this).apply {
//            isSilent = true
//            posX = 100f
//            posY = 120f
//            posZ = 0.5f
//        }
//        bouncers += Bouncer(this).apply {
//            isSilent = true
//            posX = 240f
//            posY = 110f
//            posZ = 0.65f
//        }
//        bouncers += Bouncer(this).apply {
//            isSilent = true
//            posX = 400f
//            posY = 100f
//            posZ = 0.75f
//        }
//        bouncers += Bouncer(this).apply {
//            isSilent = true
//            posX = 520f
//            posY = 75f
//            posZ = 0.85f
//        }
//        bouncers += Bouncer(this).apply {
//            isSilent = true
//            posX = 400f
//            posY = -25f
//            posZ = 0.8f
//        }
        this.bouncers = bouncers
        entities.addAll(bouncers)
    }

    fun renderUpdate(delta: Float) {
        entities.forEach {
            it.renderUpdate(delta)
        }
        entities.removeIf { it.kill }
    }

    fun render(batch: SpriteBatch) {
        camera.update()
        TMP_MATRIX.set(batch.projectionMatrix)
        batch.projectionMatrix = camera.combined
        batch.begin()
        projector.render(batch, entities)
        batch.end()
        batch.projectionMatrix = TMP_MATRIX
    }

    fun getBouncerForInput(inputType: InputType): Bouncer {
        return when (inputType) {
            InputType.A -> yellowBouncer
            InputType.DPAD -> redBouncer
        }
    }

    fun getInputTypeForBouncer(bouncer: Bouncer): InputType? {
        return if (bouncer === yellowBouncer) InputType.A else if (bouncer === redBouncer) InputType.DPAD else null
    }

    fun fireInput(inputType: InputType) {
        val bouncer = getBouncerForInput(inputType)
        val any = entities.filterIsInstance<Ball>().fold(false) { acc, it ->
            it.onInput(inputType) || acc
        }
        bouncer.bounceAnimation()
        if (!any) {
            // play dud sound
            AssetRegistry.get<Sound>("sfx_dud_${if (inputType == InputType.A) "right" else "left"}").play()
        }
    }

}