package woowacourse.movie.presentation.purchaseConfirmation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import woowacourse.movie.MovieReservationApp
import woowacourse.movie.R
import woowacourse.movie.data.MovieRepository
import woowacourse.movie.databinding.ActivityPurchaseConfirmationBinding
import woowacourse.movie.model.Reservation
import woowacourse.movie.presentation.base.BindingActivity
import woowacourse.movie.presentation.error.ErrorActivity
import kotlin.concurrent.thread

class PurchaseConfirmationActivity :
    BindingActivity<ActivityPurchaseConfirmationBinding>(R.layout.activity_purchase_confirmation),
    PurchaseConfirmationContract.View {

    private val presenter: PurchaseConfirmationContract.Presenter by lazy {
        PurchaseConfirmationPresenter(
            (applicationContext as MovieReservationApp).movieRepository,
            this
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val reservationId = intent.getLongExtra(EXTRA_RESERVATION_ID, -1)
        presenter.loadReservation(reservationId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }

    override fun showReservation(reservation: Reservation) {
        runOnUiThread {
            binding.data = reservation
            binding.reservedInformation.text =
                getString(
                    R.string.reserved_format,
                    reservation.seats.size,
                    reservation.seats.joinToString { it.row.toString() + it.number },
                    reservation.cinemaName,
                )
        }
    }

    override fun showError() {
        ErrorActivity.start(this)
        finish()
    }

    companion object {
        const val EXTRA_RESERVATION_ID = "reservationId"

        fun newIntent(
            context: Context,
            reservationId: Long,
        ): Intent {
            return Intent(context, PurchaseConfirmationActivity::class.java).apply {
                putExtra(EXTRA_RESERVATION_ID, reservationId)
            }
        }
    }
}

interface PurchaseConfirmationContract {
    interface View {
        fun showReservation(reservation: Reservation)

        fun showError()
    }

    interface Presenter {
        fun loadReservation(reservationId: Long)
    }
}

class PurchaseConfirmationPresenter(
    private val repository: MovieRepository,
    private val view: PurchaseConfirmationContract.View
) : PurchaseConfirmationContract.Presenter {
    override fun loadReservation(reservationId: Long) {
        thread {
            repository.loadReservedMovie(reservationId).onSuccess {
                view.showReservation(it)
            }.onFailure {
                view.showError()
            }
        }.join()
    }
}