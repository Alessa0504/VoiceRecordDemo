# VoiceRecordDemo
1. 仿微信语音录制与播放。
2. 组件化实现：语音录制按钮单独抽成组件。其中使用了版本管理统一版本。
3. 改造：管理不同录制状态和touch event时，使用了sealed class和livedata:
```
sealed class StateClass {
      data class StateNormal(val action: Int = ACTION_NONE) : StateClass()
      data class StatePreparing(val action: Int = ACTION_NONE) : StateClass()
      data class StateRecording(val action: Int = ACTION_NONE) : StateClass()
      data class StateWantCancel(val action: Int = ACTION_NONE) : StateClass()
}
private val recordLiveData = MutableLiveData<StateClass>()

recordLiveData.observe(context as LifecycleOwner) { state ->
            when (state) {
                is StateClass.StateNormal -> {
                    if (state.action == MotionEvent.ACTION_UP) {
                      ...
                    }
                }
                ...
            }        
            ...
}            
```

参考：https://github.com/Yintianchou/VoiceButton
