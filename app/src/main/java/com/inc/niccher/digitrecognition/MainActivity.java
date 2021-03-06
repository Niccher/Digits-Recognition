package com.inc.niccher.digitrecognition;

import android.app.Activity;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.inc.niccher.digitrecognition.models.Classification;
import com.inc.niccher.digitrecognition.models.Classifier;
import com.inc.niccher.digitrecognition.models.TensorFlowClassifier;
import com.inc.niccher.digitrecognition.views.DrawModel;
import com.inc.niccher.digitrecognition.views.DrawView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener, View.OnTouchListener {

    private static final int PIXEL_WIDTH = 28;

    private Button clearBtn, classBtn;
    private TextView resText,restflab,reskrlab,restfprob,reskrprob,restf,reskr;

    private List<Classifier> mClassifiers = new ArrayList<>();

    // views
    private DrawModel drawModel;
    private DrawView drawView;
    private PointF mTmpPiont = new PointF();

    private float mLastX;
    private float mLastY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get drawing view from XML (where the finger writes the number)
        drawView = (DrawView) findViewById(R.id.draw);
        //get the model object
        drawModel = new DrawModel(PIXEL_WIDTH, PIXEL_WIDTH);

        //init the view with the model object
        drawView.setModel(drawModel);
        // give it a touch listener to activate when the user taps
        drawView.setOnTouchListener(this);

        clearBtn = (Button) findViewById(R.id.btn_clear);
        clearBtn.setOnClickListener(this);

        classBtn = (Button) findViewById(R.id.btn_class);
        classBtn.setOnClickListener(this);

        //resText = (TextView) findViewById(R.id.tfRes);

        restf = (TextView) findViewById(R.id.btn_tf);
        reskr = (TextView) findViewById(R.id.btn_kr);

        restflab = (TextView) findViewById(R.id.btn_labtf);
        reskrlab = (TextView) findViewById(R.id.btn_labkr);

        restfprob = (TextView) findViewById(R.id.btn_probtf);
        reskrprob = (TextView) findViewById(R.id.btn_probkr);

        ModelInfer();
    }

    @Override
    protected void onResume() {
        drawView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        drawView.onPause();
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        //when the user clicks something
        if (view.getId() == R.id.btn_clear) {
            drawModel.clear();
            drawView.reset();
            drawView.invalidate();

            restflab.setText("");
            reskrlab.setText("");
            restfprob.setText("");
            reskrprob.setText("");
            restf.setText("");
            reskr.setText("");
            //resText.setText("");
        } else if (view.getId() == R.id.btn_class) {
            //if the user clicks the classify button
            //get the pixel data and store it in an array
            float pixels[] = drawView.getPixelData();

            //init an empty string to fill with the classification output
            String labl="", prob="",model="",answer = "";
            //for each classifier in our array
            for (Classifier classifier : mClassifiers) {
                //perform classification on the image
                final Classification res = classifier.recognize(pixels);
                //if it can't classify, output a question mark
                if (res.getLabel() == null) {
                    //text += classifier.name() + ": ?\n";
                } else {
                    /*model=String.format("%s",classifier.name());
                    labl=String.format("%s",res.getLabel());
                    prob=String.format("%f",res.getConf());*/
                    answer+= String.format("%s: %s, %f#", classifier.name(), res.getLabel(), res.getConf());
                    Log.e("Answer ******", String.valueOf(answer) );
                    //answer="";
                }
            }

            Splitt(answer);

            restf.setText("Tensorflow");
            reskr.setText("Keras");

        }
    }

    private void Splitt(String CutMe){
        int iend = CutMe.indexOf("#");

        String ans1,ans2;
        if (iend != -1) {
            ans1= CutMe.substring(0 , iend);
            System.out.println(ans1);

            ans2= CutMe.substring(iend+1 , CutMe.length());
            System.out.println(ans2);

            Log.e("Answer 1", String.valueOf(ans1) );
            Log.e("Answer 2", String.valueOf(ans2) );

            String remov1="Tensorflow: ";
            String remov2=" ";
            String remov3="Keras: ";
            String remov4="#";

            String answer1=(ans1.replace(remov1,"").replace(remov2, ""));
            String real_label1,real_prob1;

            String answer2=(ans2.replace(remov3,"").replace(remov2, "").replace(remov4, ""));
            String real_label2,real_prob2;

            Log.e("Trimmed 1", String.valueOf(answer1) );
            Log.e("Trimmed 2", String.valueOf(answer2) );


            int iend1=answer1.indexOf(",");
            int iend2=answer2.indexOf(",");
            float percentme;

            if (iend1 != -1) {
                real_label1= answer1.substring(0 , iend2);
                restflab.setText(real_label1);

                real_prob1= answer1.substring(iend2+1 , answer1.length());
                percentme=(Float.parseFloat(real_prob1)*100);
                restfprob.setText(String.valueOf(percentme));
            }

            if (iend2 != -1) {
                real_label2= answer2.substring(0 , iend1);
                reskrlab.setText(real_label2);

                real_prob2= answer2.substring(iend1+1 , answer2.length());
                percentme=(Float.parseFloat(real_prob2)*100);
                reskrprob.setText(String.valueOf(percentme));
            }
        }

    }

    @Override
    //this method detects which direction a user is moving their
    // finger and draws a line accordingly in that direction
    public boolean onTouch(View v, MotionEvent event) {
        //get the action and store it as an int
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        //actions have predefined ints, lets match
        //to detect, if the user has touched, which direction the users finger is moving, and if they've stopped moving

        if (action == MotionEvent.ACTION_DOWN) {
            //begin drawing line
            processTouchDown(event);
            return true;
            //draw line in every direction the user moves
        } else if (action == MotionEvent.ACTION_MOVE) {
            processTouchMove(event);
            return true;
            //if finger is lifted, stop drawing
        } else if (action == MotionEvent.ACTION_UP) {
            processTouchUp();
            return true;
        }
        return false;
    }

    private void processTouchDown(MotionEvent event) {
        //calculate the x, y coordinates where the user has touched
        mLastX = event.getX();
        mLastY = event.getY();
        //user them to calcualte the position
        drawView.calcPos(mLastX, mLastY, mTmpPiont);
        //store them in memory to draw a line between the difference in positions
        float lastConvX = mTmpPiont.x;
        float lastConvY = mTmpPiont.y;
        //and begin the line drawing
        drawModel.startLine(lastConvX, lastConvY);
    }

    //the main drawing function it actually stores all the drawing positions into the drawmodel object
    //we actually render the drawing from that object in the drawrenderer class
    private void processTouchMove(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        drawView.calcPos(x, y, mTmpPiont);
        float newConvX = mTmpPiont.x;
        float newConvY = mTmpPiont.y;
        drawModel.addLineElem(newConvX, newConvY);

        mLastX = x;
        mLastY = y;
        drawView.invalidate();
    }

    private void processTouchUp() {
        drawModel.endLine();
    }

    private void ModelInfer() {
        //The Runnable interface is another way in which you can implement multi-threading other than extending the
        // //Thread class due to the fact that Java allows you to extend only one class. Runnable is just an interface,
        // //which provides the method run.
        // //Threads are implementations and use Runnable to call the method run().
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //add 2 classifiers to our classifier arraylist the tensorflow classifier and the keras classifier
                    mClassifiers.add(
                            TensorFlowClassifier.create(getAssets(), "Tensorflow",
                                    "opt_mnist_convnet-tf.pb", "labels.txt", PIXEL_WIDTH,
                                    "input", "output", true));
                    mClassifiers.add(
                            TensorFlowClassifier.create(getAssets(), "Keras",
                                    "opt_mnist_convnet-keras.pb", "labels.txt", PIXEL_WIDTH,
                                    "conv2d_1_input", "dense_2/Softmax", false));
                } catch (final Exception e) {
                    //inaccessible model files
                    throw new RuntimeException("Error initializing classifiers!", e);
                }
            }
        }).start();
    }
}
