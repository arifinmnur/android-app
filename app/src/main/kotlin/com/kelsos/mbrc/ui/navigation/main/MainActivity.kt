package com.kelsos.mbrc.ui.navigation.main

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.ShareActionProvider
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnLongClick
import com.kelsos.mbrc.R
import com.kelsos.mbrc.annotations.Connection
import com.kelsos.mbrc.annotations.PlayerState
import com.kelsos.mbrc.annotations.PlayerState.State
import com.kelsos.mbrc.annotations.Repeat
import com.kelsos.mbrc.annotations.Repeat.Mode
import com.kelsos.mbrc.constants.Const
import com.kelsos.mbrc.constants.Protocol
import com.kelsos.mbrc.constants.UserInputEventType
import com.kelsos.mbrc.data.UserAction
import com.kelsos.mbrc.domain.TrackInfo
import com.kelsos.mbrc.enums.LfmStatus
import com.kelsos.mbrc.events.MessageEvent
import com.kelsos.mbrc.events.ui.*
import com.kelsos.mbrc.events.ui.ShuffleChange.ShuffleState
import com.kelsos.mbrc.extensions.getDimens
import com.kelsos.mbrc.helper.ProgressSeekerHelper
import com.kelsos.mbrc.helper.ProgressSeekerHelper.ProgressUpdate
import com.kelsos.mbrc.helper.VolumeChangeHelper
import com.kelsos.mbrc.ui.navigation.main.MainViewPresenter
import com.kelsos.mbrc.ui.activities.BaseActivity
import com.kelsos.mbrc.ui.dialogs.RatingDialogFragment
import com.kelsos.mbrc.ui.navigation.main.MainView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import rx.functions.Action1
import timber.log.Timber
import toothpick.Toothpick
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainActivity : BaseActivity(), MainView, ProgressUpdate {


  // Injects
  @Inject lateinit var presenter: MainViewPresenter
  @Inject lateinit var progressHelper: ProgressSeekerHelper
  // Inject elements of the view
  @BindView(R.id.main_artist_label) lateinit var artistLabel: TextView
  @BindView(R.id.main_title_label) lateinit var titleLabel: TextView
  @BindView(R.id.main_label_album) lateinit var albumLabel: TextView
  @BindView(R.id.main_track_progress_current) lateinit var trackProgressCurrent: TextView
  @BindView(R.id.main_track_duration_total) lateinit var trackDuration: TextView
  @BindView(R.id.main_button_play_pause) lateinit var playPauseButton: ImageButton
  @BindView(R.id.main_volume_seeker) lateinit var volumeBar: SeekBar
  @BindView(R.id.main_track_progress_seeker) lateinit var progressBar: SeekBar
  @BindView(R.id.main_mute_button) lateinit var muteButton: ImageButton
  @BindView(R.id.main_shuffle_button) lateinit var shuffleButton: ImageButton
  @BindView(R.id.main_repeat_button) lateinit var repeatButton: ImageButton
  @BindView(R.id.main_album_cover_image_view) lateinit var albumCover: ImageView
  private var mShareActionProvider: ShareActionProvider? = null
  private var previousVol: Int = 0
  private var menu: Menu? = null


  private val progressBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
      if (fromUser && progress != previousVol) {
        val action = UserAction(Protocol.NowPlayingPosition, progress.toString())
        postAction(action)
        previousVol = progress
      }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
    }
  }
  private var volumeChangeListener: VolumeChangeHelper? = null

  private fun register() {
    this.bus.register(this, CoverChangedEvent::class.java, { this.handleCoverEvent(it) }, true)
    this.bus.register(this, ShuffleChange::class.java, { this.handleShuffleChange(it) }, true)
    this.bus.register(this, RepeatChange::class.java, { this.updateRepeatButtonState(it) }, true)
    this.bus.register(this, VolumeChange::class.java, { this.updateVolumeData(it) }, true)
    this.bus.register(this, PlayStateChange::class.java, { this.handlePlayStateChange(it) }, true)
    this.bus.register(this,
        TrackInfoChangeEvent::class.java,
        { this.handleTrackInfoChange(it) },
        true)
    this.bus.register(this,
        ConnectionStatusChangeEvent::class.java,
        { this.handleConnectionStatusChange(it) },
        true)
    this.bus.register(this, UpdatePosition::class.java, { this.handlePositionUpdate(it) }, true)
    this.bus.register(this, ScrobbleChange::class.java, { this.handleScrobbleChange(it) }, true)
    this.bus.register(this, LfmRatingChanged::class.java, { this.handleLfmLoveChange(it) }, true)
  }

  @OnClick(R.id.main_button_play_pause)
  internal fun playButtonPressed() {
    val action = UserAction(Protocol.PlayerPlayPause, true)
    postAction(action)
  }

  @OnClick(R.id.main_button_previous)
  internal fun onPreviousButtonPressed() {
    val action = UserAction(Protocol.PlayerPrevious, true)
    postAction(action)
  }

  @OnClick(R.id.main_button_next)
  internal fun onNextButtonPressed() {
    val action = UserAction(Protocol.PlayerNext, true)
    postAction(action)
  }

  @OnLongClick(R.id.main_button_play_pause)
  internal fun onPlayerStopPressed(): Boolean {
    val action = UserAction(Protocol.PlayerStop, true)
    postAction(action)
    return true
  }

  @OnClick(R.id.main_mute_button)
  internal fun onMuteButtonPressed() {
    val action = UserAction(Protocol.PlayerMute, Const.TOGGLE)
    postAction(action)
  }

  @OnClick(R.id.main_shuffle_button)
  internal fun onShuffleButtonClicked() {
    val action = UserAction(Protocol.PlayerShuffle, Const.TOGGLE)
    postAction(action)
  }

  @OnClick(R.id.main_repeat_button)
  internal fun onRepeatButtonPressed() {
    val action = UserAction(Protocol.PlayerRepeat, Const.TOGGLE)
    postAction(action)
  }

  /**
   * Posts a user action wrapped in a MessageEvent. The bus will
   * pass the MessageEvent through the Socket to the plugin.

   * @param action Any kind of UserAction available in the [Protocol]
   */
  private fun postAction(action: UserAction) {
    bus.post(MessageEvent.action(action))
  }

  private fun changeVolume(volume: Int) {
    postAction(UserAction.create(Protocol.PlayerVolume, volume))
  }

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)
    super.setup()
    volumeChangeListener = VolumeChangeHelper(Action1<Int> { this.changeVolume(it) })
    volumeBar.setOnSeekBarChangeListener(volumeChangeListener)
    progressBar.setOnSeekBarChangeListener(progressBarChangeListener)
    progressHelper.setProgressListener(this)
  }

  public override fun onStart() {
    super.onStart()
    artistLabel.isSelected = true
    titleLabel.isSelected = true
    albumLabel.isSelected = true
  }

  public override fun onResume() {
    super.onResume()
    register()
    presenter.attach(this)
    presenter.requestNowPlayingPosition()
    presenter.load()
  }

  override fun onPause() {
    super.onPause()
    presenter.detach()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.menu_lastfm_scrobble -> {
        presenter.toggleScrobbling()
        return true
      }
      R.id.menu_rating_dialog -> {
        val ratingDialog = RatingDialogFragment()
        ratingDialog.show(supportFragmentManager, "RatingDialog")
        return true
      }
      R.id.menu_lastfm_love -> {
        bus.post(MessageEvent.action(UserAction(Protocol.NowPlayingLfmRating, Const.TOGGLE)))
        return true
      }
      else -> return false
    }
  }

  public override fun onStop() {
    super.onStop()
    bus.unregister(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    this.menu = menu
    val shareItem = menu.findItem(R.id.actionbar_share)
    mShareActionProvider = MenuItemCompat.getActionProvider(shareItem) as ShareActionProvider
    mShareActionProvider!!.setShareIntent(shareIntent)
    bus.post(OnMainFragmentOptionsInflated())
    return super.onCreateOptionsMenu(menu)
  }

  private val shareIntent: Intent
    get() {
      val shareIntent = Intent(Intent.ACTION_SEND)
      shareIntent.type = "text/plain"
      val payload = String.format("Now Playing: %s - %s", artistLabel.text, titleLabel.text)
      shareIntent.putExtra(Intent.EXTRA_TEXT, payload)
      return shareIntent
    }

  private fun handleCoverEvent(cevent: CoverChangedEvent) {
    if (!cevent.available) {
      albumCover.setImageResource(R.drawable.ic_image_no_cover)
      return
    }
    val file = File(cevent.path)

    Picasso.with(this).invalidate(file)
    val dimens = getDimens()
    Picasso.with(this)
        .load(file)
        .placeholder(R.drawable.ic_image_no_cover)
        .config(Bitmap.Config.RGB_565)
        .resize(dimens, dimens)
        .centerCrop()
        .into(albumCover, object : Callback {
          override fun onSuccess() {

          }

          override fun onError() {
            Timber.v("FaILEEEEED")
          }
        })

  }

  private fun handleShuffleChange(change: ShuffleChange) {
    updateShuffleState(change.shuffleState)
  }

  override fun updateShuffleState(@ShuffleState shuffleState: String) {
    val shuffle = ShuffleChange.OFF != shuffleState
    val autoDj = ShuffleChange.AUTODJ == shuffleState

    val color = ContextCompat.getColor(this, if (shuffle) R.color.accent else R.color.button_dark)
    shuffleButton.setColorFilter(color)

    shuffleButton.setImageResource(if (autoDj) R.drawable.ic_headset_black_24dp else R.drawable.ic_shuffle_black_24dp)
  }

  private fun updateRepeatButtonState(change: RepeatChange) {
    updateRepeat(change.mode)
  }

  override fun updateRepeat(@Mode mode: String) {
    @ColorRes var colorId = R.color.accent
    @DrawableRes var resId = R.drawable.ic_repeat_black_24dp

    //noinspection StatementWithEmptyBody
    if (Repeat.ALL.equals(mode, ignoreCase = true)) {
      // Do nothing already set above
    } else if (Repeat.ONE.equals(mode, ignoreCase = true)) {
      resId = R.drawable.ic_repeat_one_black_24dp
    } else {
      colorId = R.color.button_dark
    }

    val color = ContextCompat.getColor(this, colorId)
    repeatButton.setImageResource(resId)
    repeatButton.setColorFilter(color)
  }

  private fun updateVolumeData(change: VolumeChange) {
    updateVolume(change.volume, change.isMute)
  }

  override fun updateVolume(volume: Int, mute: Boolean) {

    if (!volumeChangeListener!!.isUserChangingVolume) {
      volumeBar.progress = volume
    }

    val color = ContextCompat.getColor(this, R.color.button_dark)
    muteButton.setColorFilter(color)
    muteButton.setImageResource(if (mute) R.drawable.ic_volume_off_black_24dp else R.drawable.ic_volume_up_black_24dp)
  }

  private fun handlePlayStateChange(change: PlayStateChange) {
    updatePlayState(change.state)
  }

  override fun updatePlayState(@State state: String) {
    val accentColor = ContextCompat.getColor(this, R.color.accent)
    @DrawableRes val resId: Int
    val tag: String

    if (PlayerState.PLAYING == state) {
      resId = R.drawable.ic_pause_circle_filled_black_24dp
      tag = "Playing"
      /* Start the animation if the track is playing*/
      presenter.requestNowPlayingPosition()
      trackProgressAnimation(progressBar.progress, progressBar.max)
    } else if (PlayerState.PAUSED == state) {
      resId = R.drawable.ic_play_circle_filled_black_24dp
      tag = PAUSED
      /* Stop the animation if the track is paused*/
      progressHelper.stop()
    } else if (PlayerState.STOPPED == state) {
      resId = R.drawable.ic_play_circle_filled_black_24dp
      tag = STOPPED
      /* Stop the animation if the track is paused*/
      progressHelper.stop()
      activateStoppedState()
    } else {
      resId = R.drawable.ic_play_circle_filled_black_24dp
      tag = STOPPED
    }

    playPauseButton.setColorFilter(accentColor)
    playPauseButton.setImageResource(resId)
    playPauseButton.tag = tag
  }

  /**
   * Starts the progress animation when called. If It was previously running then it restarts it.
   */
  private fun trackProgressAnimation(current: Int, total: Int) {
    progressHelper.stop()

    val tag = playPauseButton.tag
    if (STOPPED == tag || PAUSED == tag) {
      return
    }


    progressHelper.update(current, total)
  }

  private fun activateStoppedState() {
    progressBar.progress = 0
    trackProgressCurrent.text = getString(R.string.playback_progress, 0, 0)
  }

  private fun handleTrackInfoChange(change: TrackInfoChangeEvent) {
    updateTrackInfo(change.trackInfo)
  }

  override fun updateTrackInfo(info: TrackInfo) {
    artistLabel.text = info.artist
    titleLabel.text = info.title
    albumLabel.text = if (TextUtils.isEmpty(info.year)) info.album
    else String.format("%s [%s]", info.album, info.year)

    if (mShareActionProvider != null) {
      mShareActionProvider!!.setShareIntent(shareIntent)
    }
  }

  private fun handleConnectionStatusChange(change: ConnectionStatusChangeEvent) {
    updateConnection(change.status)
  }

  override fun updateConnection(status: Int) {
    if (status == Connection.OFF) {
      progressHelper.stop()
      activateStoppedState()
    }
  }

  /**
   * Responsible for updating the displays and seekbar responsible for the display of the track
   * duration and the
   * current progress of playback
   */

  private fun handlePositionUpdate(position: UpdatePosition) {
    val total = position.total
    val current = position.current

    if (total == 0) {
      bus.post(MessageEvent(UserInputEventType.RequestPosition))
      return
    }
    var currentSeconds = current / 1000
    var totalSeconds = total / 1000

    val currentMinutes = currentSeconds / 60
    val totalMinutes = totalSeconds / 60

    currentSeconds %= 60
    totalSeconds %= 60
    val finalTotalSeconds = totalSeconds
    val finalCurrentSeconds = currentSeconds

    trackDuration.text = getString(R.string.playback_progress, totalMinutes, finalTotalSeconds)
    trackProgressCurrent.text = getString(R.string.playback_progress,
        currentMinutes,
        finalCurrentSeconds)

    progressBar.max = total
    progressBar.progress = current

    trackProgressAnimation(position.current, position.total)
  }

  private fun handleScrobbleChange(event: ScrobbleChange) {
    updateScrobbleStatus(event.isActive)
  }

  override fun updateScrobbleStatus(active: Boolean) {
    if (menu == null) {
      return
    }
    val scrobbleMenuItem = menu!!.findItem(R.id.menu_lastfm_scrobble) ?: return

    scrobbleMenuItem.isChecked = active
  }

  private fun handleLfmLoveChange(event: LfmRatingChanged) {
    updateLfmStatus(event.status)
  }

  override fun updateLfmStatus(status: LfmStatus) {
    if (menu == null) {
      return
    }
    val favoriteMenuItem = menu!!.findItem(R.id.menu_lastfm_love) ?: return

    when (status) {
      LfmStatus.LOVED -> favoriteMenuItem.setIcon(R.drawable.ic_favorite_black_24dp)
      else -> favoriteMenuItem.setIcon(R.drawable.ic_favorite_border_black_24dp)
    }
  }

  override fun active(): Int {
    return R.id.nav_home
  }

  override fun progress(position: Int, duration: Int) {
    val currentProgress = progressBar.progress / 1000
    val currentMinutes = currentProgress / 60
    val currentSeconds = currentProgress % 60

    progressBar.progress = progressBar.progress + 1000
    trackProgressCurrent.text = getString(R.string.playback_progress,
        currentMinutes,
        currentSeconds)
  }

  override fun onDestroy() {
    Toothpick.closeScope(this)
    super.onDestroy()
  }

  companion object {
    private val PAUSED = "Paused"
    private val STOPPED = "Stopped"
  }
}
