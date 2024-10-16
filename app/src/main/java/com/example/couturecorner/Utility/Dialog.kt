import android.app.Dialog
import android.content.Context
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.couturecorner.R

object Dialog {
    fun showCustomDialog(
        context: Context,
        message: String,
        positiveButtonText: String,
        negativeButtonText: String,
        lottieAnimationResId: Int,
        positiveAction: () -> Unit,
        negativeAction: () -> Unit
    ): Dialog {  // Change return type to Dialog
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.custom_dialog)

        // Set dialog message
        val tvMessage = dialog.findViewById<TextView>(R.id.dialogMessage)
        tvMessage.text = message

        // Set positive button text and action
        val btnPositive = dialog.findViewById<Button>(R.id.positiveButton)
        btnPositive.text = positiveButtonText
        btnPositive.setOnClickListener {
            positiveAction()
            dialog.dismiss()  // Dismiss the dialog
        }

        // Set negative button text and action
        val btnNegative = dialog.findViewById<Button>(R.id.negativeButton)
        btnNegative.text = negativeButtonText
        btnNegative.setOnClickListener {
            negativeAction()
            dialog.dismiss()  // Dismiss the dialog
        }

        // Set Lottie animation
        val lottieView = dialog.findViewById<LottieAnimationView>(R.id.lottieAnimationView)
        lottieView.setAnimation(lottieAnimationResId)

        // Show the dialog
        dialog.show()

        return dialog  // Return the Dialog instance
    }
}
