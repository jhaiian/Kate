package com.jhaiian.kate.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.outlined.PlaylistPlay
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.FormatAlignLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.automirrored.sharp.FormatAlignLeft
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.sharp.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.sharp.OpenInNew
import androidx.compose.material.icons.automirrored.sharp.PlaylistAdd
import androidx.compose.material.icons.automirrored.sharp.PlaylistAddCheck
import androidx.compose.material.icons.automirrored.sharp.PlaylistPlay
import androidx.compose.material.icons.automirrored.sharp.QueueMusic
import androidx.compose.material.icons.automirrored.sharp.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DensityLarge
import androidx.compose.material.icons.filled.DensitySmall
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Gradient
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material.icons.filled.HideImage
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LibraryAdd
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.SwipeDown
import androidx.compose.material.icons.filled.SwipeLeft
import androidx.compose.material.icons.filled.SwipeRight
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.AddToQueue
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.BlurOn
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.DensityLarge
import androidx.compose.material.icons.outlined.DensitySmall
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FastForward
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.Gradient
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.HeartBroken
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.HideSource
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LibraryAdd
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.LinearScale
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.MusicOff
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Radio
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SdCard
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Sell
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.SlowMotionVideo
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.SwipeDown
import androidx.compose.material.icons.outlined.SwipeLeft
import androidx.compose.material.icons.outlined.SwipeRight
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material.icons.outlined.VerticalSplit
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.outlined.ViewCarousel
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AddToQueue
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.BlurOn
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.BubbleChart
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.DensityLarge
import androidx.compose.material.icons.rounded.DensitySmall
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FormatAlignCenter
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Gesture
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HeartBroken
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material.icons.rounded.HideSource
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LibraryAdd
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LinearScale
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SelectAll
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.Subtitles
import androidx.compose.material.icons.rounded.SwipeDown
import androidx.compose.material.icons.rounded.SwipeLeft
import androidx.compose.material.icons.rounded.SwipeRight
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material.icons.rounded.VerticalSplit
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.AddPhotoAlternate
import androidx.compose.material.icons.sharp.AddToQueue
import androidx.compose.material.icons.sharp.Album
import androidx.compose.material.icons.sharp.ArrowDownward
import androidx.compose.material.icons.sharp.ArrowDropDown
import androidx.compose.material.icons.sharp.ArrowUpward
import androidx.compose.material.icons.sharp.AutoAwesome
import androidx.compose.material.icons.sharp.BarChart
import androidx.compose.material.icons.sharp.Bedtime
import androidx.compose.material.icons.sharp.Block
import androidx.compose.material.icons.sharp.BlurOn
import androidx.compose.material.icons.sharp.BookmarkAdd
import androidx.compose.material.icons.sharp.Brightness6
import androidx.compose.material.icons.sharp.BubbleChart
import androidx.compose.material.icons.sharp.BugReport
import androidx.compose.material.icons.sharp.CalendarMonth
import androidx.compose.material.icons.sharp.Category
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Close
import androidx.compose.material.icons.sharp.ColorLens
import androidx.compose.material.icons.sharp.ContentCopy
import androidx.compose.material.icons.sharp.CreateNewFolder
import androidx.compose.material.icons.sharp.CropSquare
import androidx.compose.material.icons.sharp.DarkMode
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material.icons.sharp.DeleteSweep
import androidx.compose.material.icons.sharp.DensityLarge
import androidx.compose.material.icons.sharp.DensitySmall
import androidx.compose.material.icons.sharp.Diamond
import androidx.compose.material.icons.sharp.Download
import androidx.compose.material.icons.sharp.DragHandle
import androidx.compose.material.icons.sharp.Edit
import androidx.compose.material.icons.sharp.EmojiEvents
import androidx.compose.material.icons.sharp.Explore
import androidx.compose.material.icons.sharp.FastForward
import androidx.compose.material.icons.sharp.FileDownload
import androidx.compose.material.icons.sharp.FileOpen
import androidx.compose.material.icons.sharp.FileUpload
import androidx.compose.material.icons.sharp.Folder
import androidx.compose.material.icons.sharp.FormatAlignCenter
import androidx.compose.material.icons.sharp.FormatQuote
import androidx.compose.material.icons.sharp.Fullscreen
import androidx.compose.material.icons.sharp.Gesture
import androidx.compose.material.icons.sharp.Gradient
import androidx.compose.material.icons.sharp.GraphicEq
import androidx.compose.material.icons.sharp.Groups
import androidx.compose.material.icons.sharp.HeartBroken
import androidx.compose.material.icons.sharp.HideImage
import androidx.compose.material.icons.sharp.HideSource
import androidx.compose.material.icons.sharp.HighQuality
import androidx.compose.material.icons.sharp.History
import androidx.compose.material.icons.sharp.Image
import androidx.compose.material.icons.sharp.Info
import androidx.compose.material.icons.sharp.Insights
import androidx.compose.material.icons.sharp.KeyboardArrowDown
import androidx.compose.material.icons.sharp.KeyboardArrowUp
import androidx.compose.material.icons.sharp.LibraryAdd
import androidx.compose.material.icons.sharp.LibraryMusic
import androidx.compose.material.icons.sharp.LightMode
import androidx.compose.material.icons.sharp.Lightbulb
import androidx.compose.material.icons.sharp.LinearScale
import androidx.compose.material.icons.sharp.Link
import androidx.compose.material.icons.sharp.LocalFireDepartment
import androidx.compose.material.icons.sharp.Lock
import androidx.compose.material.icons.sharp.Mic
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material.icons.sharp.MusicNote
import androidx.compose.material.icons.sharp.MusicOff
import androidx.compose.material.icons.sharp.MyLocation
import androidx.compose.material.icons.sharp.NewReleases
import androidx.compose.material.icons.sharp.NightsStay
import androidx.compose.material.icons.sharp.Palette
import androidx.compose.material.icons.sharp.Pause
import androidx.compose.material.icons.sharp.Person
import androidx.compose.material.icons.sharp.PlayArrow
import androidx.compose.material.icons.sharp.PlayCircle
import androidx.compose.material.icons.sharp.Radio
import androidx.compose.material.icons.sharp.Refresh
import androidx.compose.material.icons.sharp.Repeat
import androidx.compose.material.icons.sharp.RepeatOne
import androidx.compose.material.icons.sharp.RestartAlt
import androidx.compose.material.icons.sharp.Restore
import androidx.compose.material.icons.sharp.Schedule
import androidx.compose.material.icons.sharp.SdCard
import androidx.compose.material.icons.sharp.Search
import androidx.compose.material.icons.sharp.SearchOff
import androidx.compose.material.icons.sharp.SelectAll
import androidx.compose.material.icons.sharp.Sell
import androidx.compose.material.icons.sharp.Settings
import androidx.compose.material.icons.sharp.SettingsBackupRestore
import androidx.compose.material.icons.sharp.Share
import androidx.compose.material.icons.sharp.Shield
import androidx.compose.material.icons.sharp.Shuffle
import androidx.compose.material.icons.sharp.SkipNext
import androidx.compose.material.icons.sharp.SkipPrevious
import androidx.compose.material.icons.sharp.SlowMotionVideo
import androidx.compose.material.icons.sharp.Smartphone
import androidx.compose.material.icons.sharp.SortByAlpha
import androidx.compose.material.icons.sharp.Speed
import androidx.compose.material.icons.sharp.Style
import androidx.compose.material.icons.sharp.Subtitles
import androidx.compose.material.icons.sharp.SwipeDown
import androidx.compose.material.icons.sharp.SwipeLeft
import androidx.compose.material.icons.sharp.SwipeRight
import androidx.compose.material.icons.sharp.TaskAlt
import androidx.compose.material.icons.sharp.TextFields
import androidx.compose.material.icons.sharp.Timer
import androidx.compose.material.icons.sharp.TouchApp
import androidx.compose.material.icons.sharp.TravelExplore
import androidx.compose.material.icons.sharp.Tune
import androidx.compose.material.icons.sharp.UnfoldMore
import androidx.compose.material.icons.sharp.VerticalSplit
import androidx.compose.material.icons.sharp.Vibration
import androidx.compose.material.icons.sharp.ViewAgenda
import androidx.compose.material.icons.sharp.ViewCarousel
import androidx.compose.material.icons.sharp.VisibilityOff
import androidx.compose.material.icons.sharp.WbSunny
import androidx.compose.material.icons.sharp.Whatshot
import androidx.compose.material.icons.sharp.Wifi
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.sharp.Memory
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.Usb
import androidx.compose.material.icons.rounded.Transform
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material.icons.sharp.Transform
import androidx.compose.material.icons.sharp.Usb
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.sharp.Devices
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.sharp.VolumeOff
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.outlined.Compress
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Speaker
import androidx.compose.material.icons.outlined.SurroundSound
import androidx.compose.material.icons.outlined.Waves
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.SurroundSound
import androidx.compose.material.icons.rounded.Waves
import androidx.compose.material.icons.sharp.Compress
import androidx.compose.material.icons.sharp.Replay
import androidx.compose.material.icons.sharp.Speaker
import androidx.compose.material.icons.sharp.SurroundSound
import androidx.compose.material.icons.sharp.Waves
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.outlined.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.sharp.VolumeDown
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.BluetoothAudio
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HeadsetOff
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.NotificationsPaused
import androidx.compose.material.icons.outlined.BatterySaver
import androidx.compose.material.icons.outlined.BluetoothAudio
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.HeadsetOff
import androidx.compose.material.icons.outlined.Hearing
import androidx.compose.material.icons.outlined.NotificationsPaused
import androidx.compose.material.icons.rounded.BatterySaver
import androidx.compose.material.icons.rounded.BluetoothAudio
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.HeadsetOff
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.NotificationsPaused
import androidx.compose.material.icons.sharp.BatterySaver
import androidx.compose.material.icons.sharp.BluetoothAudio
import androidx.compose.material.icons.sharp.Headphones
import androidx.compose.material.icons.sharp.HeadsetOff
import androidx.compose.material.icons.sharp.Hearing
import androidx.compose.material.icons.sharp.NotificationsPaused
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.sharp.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.sharp.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.sharp.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.sharp.ErrorOutline
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.sharp.RadioButtonUnchecked
import androidx.compose.ui.graphics.vector.ImageVector

internal fun iconVariants(mirrored: Boolean, name: String): Array<ImageVector>? = if (mirrored) {
    when (name) {
        "ArrowForward" -> arrayOf(Icons.AutoMirrored.Filled.ArrowForward, Icons.AutoMirrored.Outlined.ArrowForward, Icons.AutoMirrored.Rounded.ArrowForward, Icons.AutoMirrored.Sharp.ArrowForward)
        "Undo" -> arrayOf(Icons.AutoMirrored.Filled.Undo, Icons.AutoMirrored.Outlined.Undo, Icons.AutoMirrored.Rounded.Undo, Icons.AutoMirrored.Sharp.Undo)
        "ArrowBack" -> arrayOf(Icons.AutoMirrored.Filled.ArrowBack, Icons.AutoMirrored.Outlined.ArrowBack, Icons.AutoMirrored.Rounded.ArrowBack, Icons.AutoMirrored.Sharp.ArrowBack)
        "FormatAlignLeft" -> arrayOf(Icons.AutoMirrored.Filled.FormatAlignLeft, Icons.AutoMirrored.Outlined.FormatAlignLeft, Icons.AutoMirrored.Rounded.FormatAlignLeft, Icons.AutoMirrored.Sharp.FormatAlignLeft)
        "KeyboardArrowLeft" -> arrayOf(Icons.AutoMirrored.Filled.KeyboardArrowLeft, Icons.AutoMirrored.Outlined.KeyboardArrowLeft, Icons.AutoMirrored.Rounded.KeyboardArrowLeft, Icons.AutoMirrored.Sharp.KeyboardArrowLeft)
        "KeyboardArrowRight" -> arrayOf(Icons.AutoMirrored.Filled.KeyboardArrowRight, Icons.AutoMirrored.Outlined.KeyboardArrowRight, Icons.AutoMirrored.Rounded.KeyboardArrowRight, Icons.AutoMirrored.Sharp.KeyboardArrowRight)
        "OpenInNew" -> arrayOf(Icons.AutoMirrored.Filled.OpenInNew, Icons.AutoMirrored.Outlined.OpenInNew, Icons.AutoMirrored.Rounded.OpenInNew, Icons.AutoMirrored.Sharp.OpenInNew)
        "PlaylistAdd" -> arrayOf(Icons.AutoMirrored.Filled.PlaylistAdd, Icons.AutoMirrored.Outlined.PlaylistAdd, Icons.AutoMirrored.Rounded.PlaylistAdd, Icons.AutoMirrored.Sharp.PlaylistAdd)
        "PlaylistAddCheck" -> arrayOf(Icons.AutoMirrored.Filled.PlaylistAddCheck, Icons.AutoMirrored.Outlined.PlaylistAddCheck, Icons.AutoMirrored.Rounded.PlaylistAddCheck, Icons.AutoMirrored.Sharp.PlaylistAddCheck)
        "PlaylistPlay" -> arrayOf(Icons.AutoMirrored.Filled.PlaylistPlay, Icons.AutoMirrored.Outlined.PlaylistPlay, Icons.AutoMirrored.Rounded.PlaylistPlay, Icons.AutoMirrored.Sharp.PlaylistPlay)
        "QueueMusic" -> arrayOf(Icons.AutoMirrored.Filled.QueueMusic, Icons.AutoMirrored.Outlined.QueueMusic, Icons.AutoMirrored.Rounded.QueueMusic, Icons.AutoMirrored.Sharp.QueueMusic)
        "VolumeUp" -> arrayOf(Icons.AutoMirrored.Filled.VolumeUp, Icons.AutoMirrored.Outlined.VolumeUp, Icons.AutoMirrored.Rounded.VolumeUp, Icons.AutoMirrored.Sharp.VolumeUp)
        "VolumeOff" -> arrayOf(Icons.AutoMirrored.Filled.VolumeOff, Icons.AutoMirrored.Outlined.VolumeOff, Icons.AutoMirrored.Rounded.VolumeOff, Icons.AutoMirrored.Sharp.VolumeOff)
        "VolumeDown" -> arrayOf(Icons.AutoMirrored.Filled.VolumeDown, Icons.AutoMirrored.Outlined.VolumeDown, Icons.AutoMirrored.Rounded.VolumeDown, Icons.AutoMirrored.Sharp.VolumeDown)
        else -> null
    }
} else {
    when (name) {
        "CheckCircle" -> arrayOf(Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, Icons.Rounded.CheckCircle, Icons.Sharp.CheckCircle)
        "ErrorOutline" -> arrayOf(Icons.Filled.ErrorOutline, Icons.Outlined.ErrorOutline, Icons.Rounded.ErrorOutline, Icons.Sharp.ErrorOutline)
        "RadioButtonUnchecked" -> arrayOf(Icons.Filled.RadioButtonUnchecked, Icons.Outlined.RadioButtonUnchecked, Icons.Rounded.RadioButtonUnchecked, Icons.Sharp.RadioButtonUnchecked)
        "Add" -> arrayOf(Icons.Filled.Add, Icons.Outlined.Add, Icons.Rounded.Add, Icons.Sharp.Add)
        "AddPhotoAlternate" -> arrayOf(Icons.Filled.AddPhotoAlternate, Icons.Outlined.AddPhotoAlternate, Icons.Rounded.AddPhotoAlternate, Icons.Sharp.AddPhotoAlternate)
        "AddToQueue" -> arrayOf(Icons.Filled.AddToQueue, Icons.Outlined.AddToQueue, Icons.Rounded.AddToQueue, Icons.Sharp.AddToQueue)
        "Album" -> arrayOf(Icons.Filled.Album, Icons.Outlined.Album, Icons.Rounded.Album, Icons.Sharp.Album)
        "ArrowDownward" -> arrayOf(Icons.Filled.ArrowDownward, Icons.Outlined.ArrowDownward, Icons.Rounded.ArrowDownward, Icons.Sharp.ArrowDownward)
        "ArrowDropDown" -> arrayOf(Icons.Filled.ArrowDropDown, Icons.Outlined.ArrowDropDown, Icons.Rounded.ArrowDropDown, Icons.Sharp.ArrowDropDown)
        "ArrowUpward" -> arrayOf(Icons.Filled.ArrowUpward, Icons.Outlined.ArrowUpward, Icons.Rounded.ArrowUpward, Icons.Sharp.ArrowUpward)
        "AutoAwesome" -> arrayOf(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, Icons.Rounded.AutoAwesome, Icons.Sharp.AutoAwesome)
        "BarChart" -> arrayOf(Icons.Filled.BarChart, Icons.Outlined.BarChart, Icons.Rounded.BarChart, Icons.Sharp.BarChart)
        "Bedtime" -> arrayOf(Icons.Filled.Bedtime, Icons.Outlined.Bedtime, Icons.Rounded.Bedtime, Icons.Sharp.Bedtime)
        "Block" -> arrayOf(Icons.Filled.Block, Icons.Outlined.Block, Icons.Rounded.Block, Icons.Sharp.Block)
        "BlurOn" -> arrayOf(Icons.Filled.BlurOn, Icons.Outlined.BlurOn, Icons.Rounded.BlurOn, Icons.Sharp.BlurOn)
        "BookmarkAdd" -> arrayOf(Icons.Filled.BookmarkAdd, Icons.Outlined.BookmarkAdd, Icons.Rounded.BookmarkAdd, Icons.Sharp.BookmarkAdd)
        "Brightness6" -> arrayOf(Icons.Filled.Brightness6, Icons.Outlined.Brightness6, Icons.Rounded.Brightness6, Icons.Sharp.Brightness6)
        "BubbleChart" -> arrayOf(Icons.Filled.BubbleChart, Icons.Outlined.BubbleChart, Icons.Rounded.BubbleChart, Icons.Sharp.BubbleChart)
        "BugReport" -> arrayOf(Icons.Filled.BugReport, Icons.Outlined.BugReport, Icons.Rounded.BugReport, Icons.Sharp.BugReport)
        "CalendarMonth" -> arrayOf(Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, Icons.Rounded.CalendarMonth, Icons.Sharp.CalendarMonth)
        "Category" -> arrayOf(Icons.Filled.Category, Icons.Outlined.Category, Icons.Rounded.Category, Icons.Sharp.Category)
        "Check" -> arrayOf(Icons.Filled.Check, Icons.Outlined.Check, Icons.Rounded.Check, Icons.Sharp.Check)
        "Close" -> arrayOf(Icons.Filled.Close, Icons.Outlined.Close, Icons.Rounded.Close, Icons.Sharp.Close)
        "ColorLens" -> arrayOf(Icons.Filled.ColorLens, Icons.Outlined.ColorLens, Icons.Rounded.ColorLens, Icons.Sharp.ColorLens)
        "ContentCopy" -> arrayOf(Icons.Filled.ContentCopy, Icons.Outlined.ContentCopy, Icons.Rounded.ContentCopy, Icons.Sharp.ContentCopy)
        "CreateNewFolder" -> arrayOf(Icons.Filled.CreateNewFolder, Icons.Outlined.CreateNewFolder, Icons.Rounded.CreateNewFolder, Icons.Sharp.CreateNewFolder)
        "CropSquare" -> arrayOf(Icons.Filled.CropSquare, Icons.Outlined.CropSquare, Icons.Rounded.CropSquare, Icons.Sharp.CropSquare)
        "DarkMode" -> arrayOf(Icons.Filled.DarkMode, Icons.Outlined.DarkMode, Icons.Rounded.DarkMode, Icons.Sharp.DarkMode)
        "DateRange" -> arrayOf(Icons.Filled.DateRange, Icons.Outlined.DateRange, Icons.Rounded.DateRange, Icons.Sharp.DateRange)
        "Delete" -> arrayOf(Icons.Filled.Delete, Icons.Outlined.Delete, Icons.Rounded.Delete, Icons.Sharp.Delete)
        "DeleteSweep" -> arrayOf(Icons.Filled.DeleteSweep, Icons.Outlined.DeleteSweep, Icons.Rounded.DeleteSweep, Icons.Sharp.DeleteSweep)
        "DensityLarge" -> arrayOf(Icons.Filled.DensityLarge, Icons.Outlined.DensityLarge, Icons.Rounded.DensityLarge, Icons.Sharp.DensityLarge)
        "DensitySmall" -> arrayOf(Icons.Filled.DensitySmall, Icons.Outlined.DensitySmall, Icons.Rounded.DensitySmall, Icons.Sharp.DensitySmall)
        "Diamond" -> arrayOf(Icons.Filled.Diamond, Icons.Outlined.Diamond, Icons.Rounded.Diamond, Icons.Sharp.Diamond)
        "Download" -> arrayOf(Icons.Filled.Download, Icons.Outlined.Download, Icons.Rounded.Download, Icons.Sharp.Download)
        "DragHandle" -> arrayOf(Icons.Filled.DragHandle, Icons.Outlined.DragHandle, Icons.Rounded.DragHandle, Icons.Sharp.DragHandle)
        "Edit" -> arrayOf(Icons.Filled.Edit, Icons.Outlined.Edit, Icons.Rounded.Edit, Icons.Sharp.Edit)
        "EmojiEvents" -> arrayOf(Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, Icons.Rounded.EmojiEvents, Icons.Sharp.EmojiEvents)
        "Explore" -> arrayOf(Icons.Filled.Explore, Icons.Outlined.Explore, Icons.Rounded.Explore, Icons.Sharp.Explore)
        "FastForward" -> arrayOf(Icons.Filled.FastForward, Icons.Outlined.FastForward, Icons.Rounded.FastForward, Icons.Sharp.FastForward)
        "FileDownload" -> arrayOf(Icons.Filled.FileDownload, Icons.Outlined.FileDownload, Icons.Rounded.FileDownload, Icons.Sharp.FileDownload)
        "FileOpen" -> arrayOf(Icons.Filled.FileOpen, Icons.Outlined.FileOpen, Icons.Rounded.FileOpen, Icons.Sharp.FileOpen)
        "FileUpload" -> arrayOf(Icons.Filled.FileUpload, Icons.Outlined.FileUpload, Icons.Rounded.FileUpload, Icons.Sharp.FileUpload)
        "Folder" -> arrayOf(Icons.Filled.Folder, Icons.Outlined.Folder, Icons.Rounded.Folder, Icons.Sharp.Folder)
        "FormatAlignCenter" -> arrayOf(Icons.Filled.FormatAlignCenter, Icons.Outlined.FormatAlignCenter, Icons.Rounded.FormatAlignCenter, Icons.Sharp.FormatAlignCenter)
        "FormatQuote" -> arrayOf(Icons.Filled.FormatQuote, Icons.Outlined.FormatQuote, Icons.Rounded.FormatQuote, Icons.Sharp.FormatQuote)
        "Fullscreen" -> arrayOf(Icons.Filled.Fullscreen, Icons.Outlined.Fullscreen, Icons.Rounded.Fullscreen, Icons.Sharp.Fullscreen)
        "Gesture" -> arrayOf(Icons.Filled.Gesture, Icons.Outlined.Gesture, Icons.Rounded.Gesture, Icons.Sharp.Gesture)
        "Gradient" -> arrayOf(Icons.Filled.Gradient, Icons.Outlined.Gradient, Icons.Rounded.Gradient, Icons.Sharp.Gradient)
        "GraphicEq" -> arrayOf(Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq, Icons.Rounded.GraphicEq, Icons.Sharp.GraphicEq)
        "Groups" -> arrayOf(Icons.Filled.Groups, Icons.Outlined.Groups, Icons.Rounded.Groups, Icons.Sharp.Groups)
        "HeartBroken" -> arrayOf(Icons.Filled.HeartBroken, Icons.Outlined.HeartBroken, Icons.Rounded.HeartBroken, Icons.Sharp.HeartBroken)
        "HideImage" -> arrayOf(Icons.Filled.HideImage, Icons.Outlined.HideImage, Icons.Rounded.HideImage, Icons.Sharp.HideImage)
        "HideSource" -> arrayOf(Icons.Filled.HideSource, Icons.Outlined.HideSource, Icons.Rounded.HideSource, Icons.Sharp.HideSource)
        "HighQuality" -> arrayOf(Icons.Filled.HighQuality, Icons.Outlined.HighQuality, Icons.Rounded.HighQuality, Icons.Sharp.HighQuality)
        "History" -> arrayOf(Icons.Filled.History, Icons.Outlined.History, Icons.Rounded.History, Icons.Sharp.History)
        "Image" -> arrayOf(Icons.Filled.Image, Icons.Outlined.Image, Icons.Rounded.Image, Icons.Sharp.Image)
        "Info" -> arrayOf(Icons.Filled.Info, Icons.Outlined.Info, Icons.Rounded.Info, Icons.Sharp.Info)
        "Insights" -> arrayOf(Icons.Filled.Insights, Icons.Outlined.Insights, Icons.Rounded.Insights, Icons.Sharp.Insights)
        "KeyboardArrowDown" -> arrayOf(Icons.Filled.KeyboardArrowDown, Icons.Outlined.KeyboardArrowDown, Icons.Rounded.KeyboardArrowDown, Icons.Sharp.KeyboardArrowDown)
        "KeyboardArrowUp" -> arrayOf(Icons.Filled.KeyboardArrowUp, Icons.Outlined.KeyboardArrowUp, Icons.Rounded.KeyboardArrowUp, Icons.Sharp.KeyboardArrowUp)
        "LibraryAdd" -> arrayOf(Icons.Filled.LibraryAdd, Icons.Outlined.LibraryAdd, Icons.Rounded.LibraryAdd, Icons.Sharp.LibraryAdd)
        "LibraryMusic" -> arrayOf(Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic, Icons.Rounded.LibraryMusic, Icons.Sharp.LibraryMusic)
        "LightMode" -> arrayOf(Icons.Filled.LightMode, Icons.Outlined.LightMode, Icons.Rounded.LightMode, Icons.Sharp.LightMode)
        "Lightbulb" -> arrayOf(Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb, Icons.Rounded.Lightbulb, Icons.Sharp.Lightbulb)
        "LinearScale" -> arrayOf(Icons.Filled.LinearScale, Icons.Outlined.LinearScale, Icons.Rounded.LinearScale, Icons.Sharp.LinearScale)
        "Link" -> arrayOf(Icons.Filled.Link, Icons.Outlined.Link, Icons.Rounded.Link, Icons.Sharp.Link)
        "LocalFireDepartment" -> arrayOf(Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment, Icons.Rounded.LocalFireDepartment, Icons.Sharp.LocalFireDepartment)
        "Lock" -> arrayOf(Icons.Filled.Lock, Icons.Outlined.Lock, Icons.Rounded.Lock, Icons.Sharp.Lock)
        "Mic" -> arrayOf(Icons.Filled.Mic, Icons.Outlined.Mic, Icons.Rounded.Mic, Icons.Sharp.Mic)
        "MoreVert" -> arrayOf(Icons.Filled.MoreVert, Icons.Outlined.MoreVert, Icons.Rounded.MoreVert, Icons.Sharp.MoreVert)
        "MusicNote" -> arrayOf(Icons.Filled.MusicNote, Icons.Outlined.MusicNote, Icons.Rounded.MusicNote, Icons.Sharp.MusicNote)
        "MusicOff" -> arrayOf(Icons.Filled.MusicOff, Icons.Outlined.MusicOff, Icons.Rounded.MusicOff, Icons.Sharp.MusicOff)
        "MyLocation" -> arrayOf(Icons.Filled.MyLocation, Icons.Outlined.MyLocation, Icons.Rounded.MyLocation, Icons.Sharp.MyLocation)
        "NewReleases" -> arrayOf(Icons.Filled.NewReleases, Icons.Outlined.NewReleases, Icons.Rounded.NewReleases, Icons.Sharp.NewReleases)
        "NightsStay" -> arrayOf(Icons.Filled.NightsStay, Icons.Outlined.NightsStay, Icons.Rounded.NightsStay, Icons.Sharp.NightsStay)
        "Palette" -> arrayOf(Icons.Filled.Palette, Icons.Outlined.Palette, Icons.Rounded.Palette, Icons.Sharp.Palette)
        "Pause" -> arrayOf(Icons.Filled.Pause, Icons.Outlined.Pause, Icons.Rounded.Pause, Icons.Sharp.Pause)
        "Person" -> arrayOf(Icons.Filled.Person, Icons.Outlined.Person, Icons.Rounded.Person, Icons.Sharp.Person)
        "PlayArrow" -> arrayOf(Icons.Filled.PlayArrow, Icons.Outlined.PlayArrow, Icons.Rounded.PlayArrow, Icons.Sharp.PlayArrow)
        "PlayCircle" -> arrayOf(Icons.Filled.PlayCircle, Icons.Outlined.PlayCircle, Icons.Rounded.PlayCircle, Icons.Sharp.PlayCircle)
        "Radio" -> arrayOf(Icons.Filled.Radio, Icons.Outlined.Radio, Icons.Rounded.Radio, Icons.Sharp.Radio)
        "Refresh" -> arrayOf(Icons.Filled.Refresh, Icons.Outlined.Refresh, Icons.Rounded.Refresh, Icons.Sharp.Refresh)
        "Repeat" -> arrayOf(Icons.Filled.Repeat, Icons.Outlined.Repeat, Icons.Rounded.Repeat, Icons.Sharp.Repeat)
        "RepeatOne" -> arrayOf(Icons.Filled.RepeatOne, Icons.Outlined.RepeatOne, Icons.Rounded.RepeatOne, Icons.Sharp.RepeatOne)
        "RestartAlt" -> arrayOf(Icons.Filled.RestartAlt, Icons.Outlined.RestartAlt, Icons.Rounded.RestartAlt, Icons.Sharp.RestartAlt)
        "Restore" -> arrayOf(Icons.Filled.Restore, Icons.Outlined.Restore, Icons.Rounded.Restore, Icons.Sharp.Restore)
        "Schedule" -> arrayOf(Icons.Filled.Schedule, Icons.Outlined.Schedule, Icons.Rounded.Schedule, Icons.Sharp.Schedule)
        "SdCard" -> arrayOf(Icons.Filled.SdCard, Icons.Outlined.SdCard, Icons.Rounded.SdCard, Icons.Sharp.SdCard)
        "Search" -> arrayOf(Icons.Filled.Search, Icons.Outlined.Search, Icons.Rounded.Search, Icons.Sharp.Search)
        "SearchOff" -> arrayOf(Icons.Filled.SearchOff, Icons.Outlined.SearchOff, Icons.Rounded.SearchOff, Icons.Sharp.SearchOff)
        "SelectAll" -> arrayOf(Icons.Filled.SelectAll, Icons.Outlined.SelectAll, Icons.Rounded.SelectAll, Icons.Sharp.SelectAll)
        "Sell" -> arrayOf(Icons.Filled.Sell, Icons.Outlined.Sell, Icons.Rounded.Sell, Icons.Sharp.Sell)
        "Settings" -> arrayOf(Icons.Filled.Settings, Icons.Outlined.Settings, Icons.Rounded.Settings, Icons.Sharp.Settings)
        "SettingsBackupRestore" -> arrayOf(Icons.Filled.SettingsBackupRestore, Icons.Outlined.SettingsBackupRestore, Icons.Rounded.SettingsBackupRestore, Icons.Sharp.SettingsBackupRestore)
        "Share" -> arrayOf(Icons.Filled.Share, Icons.Outlined.Share, Icons.Rounded.Share, Icons.Sharp.Share)
        "Shield" -> arrayOf(Icons.Filled.Shield, Icons.Outlined.Shield, Icons.Rounded.Shield, Icons.Sharp.Shield)
        "Shuffle" -> arrayOf(Icons.Filled.Shuffle, Icons.Outlined.Shuffle, Icons.Rounded.Shuffle, Icons.Sharp.Shuffle)
        "SkipNext" -> arrayOf(Icons.Filled.SkipNext, Icons.Outlined.SkipNext, Icons.Rounded.SkipNext, Icons.Sharp.SkipNext)
        "SkipPrevious" -> arrayOf(Icons.Filled.SkipPrevious, Icons.Outlined.SkipPrevious, Icons.Rounded.SkipPrevious, Icons.Sharp.SkipPrevious)
        "SlowMotionVideo" -> arrayOf(Icons.Filled.SlowMotionVideo, Icons.Outlined.SlowMotionVideo, Icons.Rounded.SlowMotionVideo, Icons.Sharp.SlowMotionVideo)
        "Smartphone" -> arrayOf(Icons.Filled.Smartphone, Icons.Outlined.Smartphone, Icons.Rounded.Smartphone, Icons.Sharp.Smartphone)
        "SortByAlpha" -> arrayOf(Icons.Filled.SortByAlpha, Icons.Outlined.SortByAlpha, Icons.Rounded.SortByAlpha, Icons.Sharp.SortByAlpha)
        "Speed" -> arrayOf(Icons.Filled.Speed, Icons.Outlined.Speed, Icons.Rounded.Speed, Icons.Sharp.Speed)
        "Style" -> arrayOf(Icons.Filled.Style, Icons.Outlined.Style, Icons.Rounded.Style, Icons.Sharp.Style)
        "Subtitles" -> arrayOf(Icons.Filled.Subtitles, Icons.Outlined.Subtitles, Icons.Rounded.Subtitles, Icons.Sharp.Subtitles)
        "SwipeDown" -> arrayOf(Icons.Filled.SwipeDown, Icons.Outlined.SwipeDown, Icons.Rounded.SwipeDown, Icons.Sharp.SwipeDown)
        "SwipeLeft" -> arrayOf(Icons.Filled.SwipeLeft, Icons.Outlined.SwipeLeft, Icons.Rounded.SwipeLeft, Icons.Sharp.SwipeLeft)
        "SwipeRight" -> arrayOf(Icons.Filled.SwipeRight, Icons.Outlined.SwipeRight, Icons.Rounded.SwipeRight, Icons.Sharp.SwipeRight)
        "TaskAlt" -> arrayOf(Icons.Filled.TaskAlt, Icons.Outlined.TaskAlt, Icons.Rounded.TaskAlt, Icons.Sharp.TaskAlt)
        "TextFields" -> arrayOf(Icons.Filled.TextFields, Icons.Outlined.TextFields, Icons.Rounded.TextFields, Icons.Sharp.TextFields)
        "Timer" -> arrayOf(Icons.Filled.Timer, Icons.Outlined.Timer, Icons.Rounded.Timer, Icons.Sharp.Timer)
        "TouchApp" -> arrayOf(Icons.Filled.TouchApp, Icons.Outlined.TouchApp, Icons.Rounded.TouchApp, Icons.Sharp.TouchApp)
        "TravelExplore" -> arrayOf(Icons.Filled.TravelExplore, Icons.Outlined.TravelExplore, Icons.Rounded.TravelExplore, Icons.Sharp.TravelExplore)
        "Tune" -> arrayOf(Icons.Filled.Tune, Icons.Outlined.Tune, Icons.Rounded.Tune, Icons.Sharp.Tune)
        "UnfoldMore" -> arrayOf(Icons.Filled.UnfoldMore, Icons.Outlined.UnfoldMore, Icons.Rounded.UnfoldMore, Icons.Sharp.UnfoldMore)
        "VerticalSplit" -> arrayOf(Icons.Filled.VerticalSplit, Icons.Outlined.VerticalSplit, Icons.Rounded.VerticalSplit, Icons.Sharp.VerticalSplit)
        "Vibration" -> arrayOf(Icons.Filled.Vibration, Icons.Outlined.Vibration, Icons.Rounded.Vibration, Icons.Sharp.Vibration)
        "ViewAgenda" -> arrayOf(Icons.Filled.ViewAgenda, Icons.Outlined.ViewAgenda, Icons.Rounded.ViewAgenda, Icons.Sharp.ViewAgenda)
        "ViewCarousel" -> arrayOf(Icons.Filled.ViewCarousel, Icons.Outlined.ViewCarousel, Icons.Rounded.ViewCarousel, Icons.Sharp.ViewCarousel)
        "VisibilityOff" -> arrayOf(Icons.Filled.VisibilityOff, Icons.Outlined.VisibilityOff, Icons.Rounded.VisibilityOff, Icons.Sharp.VisibilityOff)
        "WbSunny" -> arrayOf(Icons.Filled.WbSunny, Icons.Outlined.WbSunny, Icons.Rounded.WbSunny, Icons.Sharp.WbSunny)
        "Whatshot" -> arrayOf(Icons.Filled.Whatshot, Icons.Outlined.Whatshot, Icons.Rounded.Whatshot, Icons.Sharp.Whatshot)
        "Wifi" -> arrayOf(Icons.Filled.Wifi, Icons.Outlined.Wifi, Icons.Rounded.Wifi, Icons.Sharp.Wifi)
        "Memory" -> arrayOf(Icons.Filled.Memory, Icons.Outlined.Memory, Icons.Rounded.Memory, Icons.Sharp.Memory)
        "Transform" -> arrayOf(Icons.Filled.Transform, Icons.Outlined.Transform, Icons.Rounded.Transform, Icons.Sharp.Transform)
        "Usb" -> arrayOf(Icons.Filled.Usb, Icons.Outlined.Usb, Icons.Rounded.Usb, Icons.Sharp.Usb)
        "Devices" -> arrayOf(Icons.Filled.Devices, Icons.Outlined.Devices, Icons.Rounded.Devices, Icons.Sharp.Devices)
        "Compress" -> arrayOf(Icons.Filled.Compress, Icons.Outlined.Compress, Icons.Rounded.Compress, Icons.Sharp.Compress)
        "Replay" -> arrayOf(Icons.Filled.Replay, Icons.Outlined.Replay, Icons.Rounded.Replay, Icons.Sharp.Replay)
        "Speaker" -> arrayOf(Icons.Filled.Speaker, Icons.Outlined.Speaker, Icons.Rounded.Speaker, Icons.Sharp.Speaker)
        "SurroundSound" -> arrayOf(Icons.Filled.SurroundSound, Icons.Outlined.SurroundSound, Icons.Rounded.SurroundSound, Icons.Sharp.SurroundSound)
        "Waves" -> arrayOf(Icons.Filled.Waves, Icons.Outlined.Waves, Icons.Rounded.Waves, Icons.Sharp.Waves)
        "BatterySaver" -> arrayOf(Icons.Filled.BatterySaver, Icons.Outlined.BatterySaver, Icons.Rounded.BatterySaver, Icons.Sharp.BatterySaver)
        "BluetoothAudio" -> arrayOf(Icons.Filled.BluetoothAudio, Icons.Outlined.BluetoothAudio, Icons.Rounded.BluetoothAudio, Icons.Sharp.BluetoothAudio)
        "Headphones" -> arrayOf(Icons.Filled.Headphones, Icons.Outlined.Headphones, Icons.Rounded.Headphones, Icons.Sharp.Headphones)
        "HeadsetOff" -> arrayOf(Icons.Filled.HeadsetOff, Icons.Outlined.HeadsetOff, Icons.Rounded.HeadsetOff, Icons.Sharp.HeadsetOff)
        "Hearing" -> arrayOf(Icons.Filled.Hearing, Icons.Outlined.Hearing, Icons.Rounded.Hearing, Icons.Sharp.Hearing)
        "NotificationsPaused" -> arrayOf(Icons.Filled.NotificationsPaused, Icons.Outlined.NotificationsPaused, Icons.Rounded.NotificationsPaused, Icons.Sharp.NotificationsPaused)
        else -> null
    }
}
