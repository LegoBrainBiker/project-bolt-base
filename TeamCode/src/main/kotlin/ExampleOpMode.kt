import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime

@TeleOp(name = "Example OpMode", group = "Concept")
@Disabled // TODO: Remove this line to enable the OpMode
class ExampleOpMode : OpMode() {
    private val runtime = ElapsedTime()

    /**
     * This method will be called once, when the INIT button is pressed.
     */
    override fun init() {
        telemetry.addData("Status", "Initialized")
    }

    /**
     * This method will be called repeatedly during the period between when
     * the INIT button is pressed and when the START button is pressed (or the
     * OpMode is stopped).
     */
    override fun init_loop() {
    }

    /**
     * This method will be called once, when the START button is pressed.
     */
    override fun start() {
        runtime.reset()
    }

    /**
     * This method will be called repeatedly during the period between when
     * the START button is pressed and when the OpMode is stopped.
     */
    override fun loop() {
        telemetry.addData("Status", "Run Time: $runtime")
    }

    /**
     * This method will be called once, when this OpMode is stopped.
     *
     *
     * Your ability to control hardware from this method will be limited.
     */
    override fun stop() {
    }
}