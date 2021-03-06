package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import java.util.List;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.ArrayList;
import java.util.List;

import static org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XYZ;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesOrder.XZY;
import static org.firstinspires.ftc.robotcore.external.navigation.AxesReference.EXTRINSIC;



@Autonomous(name="BlueStorageStorage", group="linearOpMode")
public class BlueStorageStorage extends AutonomousPrime2021 {

    /*
     ***********************
     *   SETUP TENSORFLOW  *
     ***********************
     */
    private static final String TFOD_MODEL_ASSET = "FreightFrenzy_BCDM.tflite";
    private static final String[] LABELS = {
            "Ball",
            "Cube",
            "Duck",
            "Marker"
    };
    private static final String VUFORIA_KEY = "Aba+gBH/////AAABma/0sYDZakYVhtjb1kH5oBVmYfYsDZXTuEZL9m7EdnFKZN/0v/LvE/Yr0NsXiJo0mJmznKAA5MK6ojvgtV1e1ODodBaMYZpgE1YeoAXYpvvPGEsdGv3xbvgKhvwOvqDToPe3x5w6gsq7a4Ullp76kLxRIoZAqaRpOuf1/tiJJQ7gTBFf8MKgbCDosmMDj7FOZsclk7kos4L46bLkVBcD9E0l7tNR0H0ShiOvxBwq5eDvzvmzsjeGc1aPgx9Br5AbUwN1T+BOvqwvZH2pM2HDbybgcWQJKH1YvXH4O62ENsYhD9ubvktayK8hSuu2CpUd1FVU3YQp91UrCvaKPYMiMFu7zeQCnoc7UOpG1P/kdFKP";
    private static String labelName;
    private static int noLabel;
    private TFObjectDetector tfod;

    double DuckRightPos = -1;
    int DuckPosition = 0;

    /*
     ********************
     *   SETUP VUFORIA  *
     ********************
     */
    private static final float mmPerInch        = 25.4f;
    private static final float mmTargetHeight   = 6 * mmPerInch;          // the height of the center of the target image above the floor
    private static final float halfField        = 72 * mmPerInch;
    private static final float halfTile         = 12 * mmPerInch;
    private static final float oneAndHalfTile   = 36 * mmPerInch;

    // Class Members
    private OpenGLMatrix lastLocation   = null;
    private VuforiaLocalizer vuforia    = null;
    private VuforiaTrackables targets   = null ;
    private WebcamName webcamName       = null;

    private boolean targetVisible       = false;
    List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();

    double VufXPos = 0;
    double VufYPos = 0;
    double VufHeading = 0;


    @Override
    public void runOpMode(){
        webcamName = hardwareMap.get(WebcamName.class, "Webcam");
        initVuforia();
        initTfod();
        tfod.activate();
        tfodTrack();

        mapObjects();
        waitForStart();



        tfodTrack();

        //Middle Pos
        if(DuckRightPos>=164&&DuckRightPos<=364) {
            DuckPosition=1;
            telemetry.addData("Duck Position: ", DuckPosition);
            telemetry.update();
        }
        //Right Pos
        else if(DuckRightPos>=463&&DuckRightPos<=663){
            DuckPosition=2;
            telemetry.addData("Duck Position: ", DuckPosition);
            telemetry.update();
        }
        //Left Pos
        else{
            telemetry.addData("Duck Position: ", DuckPosition);
            telemetry.update();
        }

        //Spin duck

        strafeRightEncoder(22, 0.5);
        rightEncoder(100,0.5);

        dArm(6,0.4);

        double idealWobblePos=12.5; //was 16.4, 12.5, 3.5, 7.5,
        updateRightDist();
        int count = 0;
        while(RightDist==0 || RightDist>800){
            updateRightDist();
            telemetry.addData("Getting valid dist sensor ", "info");
            telemetry.update();
            count++;
            if(count>25){
                strafeRightEncoder(25,0.5);
                count=0;
            }
        }
        strafeRightEncoder(RightDist-idealWobblePos,0.25);
        //reverseEncoder(46, 0.5);


        //Need to figure out servo values for duck spin
        duckSpin(1,4);


        forwardEncoder(65,0.5); //Was 45, 35, 38, 48, 53, 58, 68, 65

        //strafeLeftEncoder(15,0.5);


        //leftEncoder(190,0.5); //was 180, 200,
        zeroBotEncoderOffset(85, 0.5); //was 90, 120, 70, 80, 90, 95

        dArm(0,0.45);

        //spinIntake(1);

        strafeRightEncoder(70,0.5);


        if(DuckPosition == 0) {
            /*strafeLeftEncoder(45, 0.5);
            dArmWait(-5,0.25);
            strafeLeftEncoder(40, 0.5);*/
        } else if(DuckPosition == 1) {
            /*strafeLeftEncoder(20, 0.5);
            dArmWait(-5,0.25);
            strafeLeftEncoder(65, 0.5);*/
        } else if(DuckPosition == 2){
            /*dArmWait(-5,0.25);
            strafeLeftEncoder(85, 0.5);*/
            //This arm is in no way affiliated with Danny McDowell.
            //Any complaints should be filed to Gustav Nelson.
        }

        /*strafeRightEncoder(50, 0.5);
        pause(1);
        vuforiaTrack();
        telemetry.update();


        if(targetVisible) {
            if(VufHeading>=0&&VufHeading<180){
                rightEncoder(VufHeading*1, 0.15);
            }
            else if (VufHeading>=180&&VufHeading<360){
                leftEncoder((360-VufHeading*1), 0.15);
            }

            vuforiaTrack();

            double ParkXPos = -60; //-50 (too far "south"), -55, -65 (too far "north"), -60
            forwardEncoder(VufXPos - ParkXPos, 0.25);
            double ParkYPos = 45; //Update with position goal 40.9, then 35
            strafeLeftEncoder(VufYPos - ParkYPos, 0.25);
        }
        else{
            strafeRightEncoder(50,0.5);
        }*/










    }

    private void initVuforia() {

        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters(cameraMonitorViewId);

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = webcamName;
        parameters.useExtendedTracking = false;


        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the TensorFlow Object Detection engine; but it is now!
        targets = this.vuforia.loadTrackablesFromAsset("FreightFrenzy");
        allTrackables.addAll(targets);

        identifyTarget(0, "Blue Storage",       -halfField,  oneAndHalfTile, mmTargetHeight, 90, 0, 90);
        identifyTarget(1, "Blue Alliance Wall",  halfTile,   halfField,      mmTargetHeight, 90, 0, 0);
        identifyTarget(2, "Red Storage",        -halfField, -oneAndHalfTile, mmTargetHeight, 90, 0, 90);
        identifyTarget(3, "Red Alliance Wall",   halfTile,  -halfField,      mmTargetHeight, 90, 0, 180);

        final float CAMERA_FORWARD_DISPLACEMENT  = 0.0f * mmPerInch;   // eg: Enter the forward distance from the center of the robot to the camera lens
        final float CAMERA_VERTICAL_DISPLACEMENT = 8.9f * mmPerInch;   // eg: Camera is 6 Inches above ground
        final float CAMERA_LEFT_DISPLACEMENT     = 0.0f * mmPerInch;   // eg: Enter the left distance from the center of the robot to the camera lens

        OpenGLMatrix cameraLocationOnRobot = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT, CAMERA_LEFT_DISPLACEMENT, CAMERA_VERTICAL_DISPLACEMENT)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XZY, DEGREES, 90, 90, 0));

        for (VuforiaTrackable trackable : allTrackables) {
            ((VuforiaTrackableDefaultListener) trackable.getListener()).setCameraLocationOnRobot(parameters.cameraName, cameraLocationOnRobot);
        }

        targets.activate();


    }

    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfodParameters.minResultConfidence = 0.85f;
        tfodParameters.isModelTensorFlow2 = true;
        tfodParameters.inputSize = 320;
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABELS);
    }

    void    identifyTarget(int targetIndex, String targetName, float dx, float dy, float dz, float rx, float ry, float rz) {
        VuforiaTrackable aTarget = targets.get(targetIndex);
        aTarget.setName(targetName);
        aTarget.setLocation(OpenGLMatrix.translation(dx, dy, dz)
                .multiplied(Orientation.getRotationMatrix(EXTRINSIC, XYZ, DEGREES, rx, ry, rz)));
    }

    private void vuforiaTrack(){
        targetVisible = false;
        for (VuforiaTrackable trackable : allTrackables) {
            if (((VuforiaTrackableDefaultListener)trackable.getListener()).isVisible()) {
                telemetry.addData("Visible Target", trackable.getName());
                targetVisible = true;

                // getUpdatedRobotLocation() will return null if no new information is available since
                // the last time that call was made, or if the trackable is not currently visible.
                OpenGLMatrix robotLocationTransform = ((VuforiaTrackableDefaultListener)trackable.getListener()).getUpdatedRobotLocation();
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform;
                }
                break;
            }
        }

        // Provide feedback as to where the robot is located (if we know).
        if (targetVisible) {
            // express position (translation) of robot in inches.
            VectorF translation = lastLocation.getTranslation();
            telemetry.addData("Pos (inches)", "{X, Y, Z} = %.1f, %.1f, %.1f",
                    translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch);
            VufXPos=translation.get(0) / mmPerInch;
            VufYPos=translation.get(2) / mmPerInch;

            // express the rotation of the robot in degrees.
            Orientation rotation = Orientation.getOrientation(lastLocation, EXTRINSIC, XYZ, DEGREES);
            telemetry.addData("Rot (deg)", "{Roll, Pitch, Heading} = %.0f, %.0f, %.0f", rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle + 180);
            VufHeading = rotation.thirdAngle + 180;
        }
        else {
            telemetry.addData("Visible Target", "none");
        }
    }

    private void tfodTrack(){
        if (tfod != null) {
            List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
            if (updatedRecognitions != null) {
                telemetry.addData("# Object Detected", updatedRecognitions.size());
                noLabel  = updatedRecognitions.size();
                int i = 0;
                for (Recognition recognition : updatedRecognitions) {
                    telemetry.addData(String.format("label (%d)", i), recognition.getLabel());
                    telemetry.addData(String.format("  left,top (%d)", i), "%.03f , %.03f",
                            recognition.getLeft(), recognition.getTop());
                    telemetry.addData(String.format("  right,bottom (%d)", i), "%.03f , %.03f",
                            recognition.getRight(), recognition.getBottom());
                    labelName = recognition.getLabel();
                    if(labelName.equals("Duck")){
                        DuckRightPos=recognition.getRight();
                    }

                }


            }
        }
    }


}
