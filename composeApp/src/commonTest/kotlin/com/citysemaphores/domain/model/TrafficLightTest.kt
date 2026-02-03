package com.citysemaphores.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TrafficLightTest {

    @Test
    fun trafficLightStateToggleShouldSwitchBetweenRedAndGreen() {
        assertEquals(TrafficLightState.GREEN, TrafficLightState.RED.toggle())
        assertEquals(TrafficLightState.RED, TrafficLightState.GREEN.toggle())
    }

    @Test
    fun trafficLightStateCanPassShouldReturnTrueOnlyForGreen() {
        assertTrue(TrafficLightState.GREEN.canPass())
        assertFalse(TrafficLightState.RED.canPass())
    }

    @Test
    fun trafficLightToggleShouldSwitchState() {
        val redLight = TrafficLight(Direction.NORTH, TrafficLightState.RED)
        val greenLight = redLight.toggle()

        assertEquals(TrafficLightState.GREEN, greenLight.state)
        assertEquals(TrafficLightState.RED, greenLight.toggle().state)
    }

    @Test
    fun trafficLightCanPassShouldReflectState() {
        val redLight = TrafficLight(Direction.NORTH, TrafficLightState.RED)
        val greenLight = TrafficLight(Direction.NORTH, TrafficLightState.GREEN)

        assertFalse(redLight.canPass())
        assertTrue(greenLight.canPass())
    }

    @Test
    fun trafficLightSetGreenAndSetRedShouldWorkCorrectly() {
        val light = TrafficLight(Direction.SOUTH)

        val greenLight = light.setGreen()
        assertEquals(TrafficLightState.GREEN, greenLight.state)

        val redLight = greenLight.setRed()
        assertEquals(TrafficLightState.RED, redLight.state)
    }

    @Test
    fun trafficLightShouldPreserveDirectionWhenToggling() {
        val light = TrafficLight(Direction.EAST, TrafficLightState.RED)
        val toggled = light.toggle()

        assertEquals(Direction.EAST, toggled.direction)
    }
}
