package com.smart.autodaily.handler

import androidx.collection.mutableIntSetOf
import com.smart.autodaily.command.AdbBack
import com.smart.autodaily.command.AdbClick
import com.smart.autodaily.command.AdbPartClick
import com.smart.autodaily.command.AdbSwipe
import com.smart.autodaily.command.AddPosById
import com.smart.autodaily.command.DropdownMenuNext
import com.smart.autodaily.command.FinishFlowId
import com.smart.autodaily.command.MinusPosById
import com.smart.autodaily.command.NotFlowId
import com.smart.autodaily.command.Operation
import com.smart.autodaily.command.Reboot
import com.smart.autodaily.command.RelFAC
import com.smart.autodaily.command.RelLabFAC
import com.smart.autodaily.command.Return
import com.smart.autodaily.command.RmSkipAcId
import com.smart.autodaily.command.RmSkipAcIdList
import com.smart.autodaily.command.RmSkipFlowId
import com.smart.autodaily.command.Skip
import com.smart.autodaily.command.SkipAcId
import com.smart.autodaily.command.SkipFlowId
import com.smart.autodaily.command.Sleep
import com.smart.autodaily.constant.ActionString
import com.smart.autodaily.data.appDb
import com.smart.autodaily.data.entity.Point
import com.smart.autodaily.data.entity.Rect
import com.smart.autodaily.data.entity.ScriptActionInfo
import com.smart.autodaily.utils.Lom
import com.smart.autodaily.utils.ScreenCaptureUtil
import com.smart.autodaily.utils.SnackbarUtil
import com.smart.autodaily.utils.runScope
import kotlinx.coroutines.cancelChildren
import splitties.init.appCtx

//操作映射为函数
fun initActionFun(scriptActionInfo: ScriptActionInfo){
    try {
        scriptActionInfo.addFlag = true
        scriptActionInfo.actionString.split(";").forEach {  action->
            when(action){
                ActionString.CLICK-> {
                    scriptActionInfo.command.add (Operation( 1, AdbClick()) )
                }
                ActionString.CLICKC-> {
                    val x = (ScreenCaptureUtil.getDisplayMetrics(appCtx).widthPixels/2 + conf.random).toInt()
                    val y = (ScreenCaptureUtil.getDisplayMetrics(appCtx).heightPixels/2 + conf.random).toInt()
                    scriptActionInfo.command.add (Operation( 2, AdbClick(Point(x,y))) )
                }
                ActionString.FINISH ->{
                    scriptActionInfo.command.add (Return(ActionString.FINISH))
                }
                ActionString.SKIP ->{
                    scriptActionInfo.command.add(Skip())
                }
                ActionString.SLEEP -> {
                    scriptActionInfo.command.add(Sleep())
                }
                ActionString.JUMP ->{
                    scriptActionInfo.command.add(Return(ActionString.JUMP))
                }
                ActionString.BACK ->{
                    scriptActionInfo.command.add(AdbBack())
                }
                ActionString.REBOOT ->{
                    scriptActionInfo.command.add(Reboot(conf.pkgName))
                }
                else ->{
                    val dm = ScreenCaptureUtil.getDisplayMetrics(appCtx)
                    when{
                        action.startsWith(  ActionString.SLEEP  ) -> {
                            val sleepTime = action.substring(   ActionString.SLEEP.length+1, action.length-1   ).toLong()
                            scriptActionInfo.command.add(Sleep(sleepTime))
                        }
                        action.startsWith(  ActionString.FINISH  ) -> {
                            val flowId = action.substring(   ActionString.FINISH.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(FinishFlowId(flowId))
                        }
                        action.startsWith(  ActionString.SKIP_FLOW_ID  ) -> {
                            val flowId = action.substring(   ActionString.SKIP_FLOW_ID.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(SkipFlowId(flowId))
                        }
                        action.startsWith( ActionString.CLICK_PART ) ->{
                            val type = if(action.contains("x")){"x"}else{"y"}
                            val lastIdx =  action.indexOfLast { it==',' }
                            val part = action.substring(   ActionString.CLICK_PART.length+3, lastIdx ).toInt()
                            val idx = action.substring(action.indexOfLast { it==',' }+1, action.length-1 ).toInt()
                            scriptActionInfo.command.add(Operation(1, AdbPartClick(type, part, idx)))
                        }
                        action.startsWith( ActionString.VER_SWIPE ) ->{
                            val type =  action.substring(   ActionString.VER_SWIPE.length ).toInt()
                            when(type){
                                0 -> {
                                    val x =  (dm.widthPixels/2 ).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 3).toFloat(), x,  (dm.heightPixels/8 * 5).toFloat() )
                                }
                                9 ->{
                                    val x =  (dm.widthPixels/2 ).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 5).toFloat(), x,  (dm.heightPixels/8 * 3).toFloat() )
                                }
                                1 -> {
                                    val x =  (dm.widthPixels/4).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 3).toFloat(), x,  (dm.heightPixels/8 * 5).toFloat()  )
                                }
                                2 ->{
                                    val x =  (dm.widthPixels/4).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x,  (dm.heightPixels/8 * 5).toFloat(), x,  (dm.heightPixels/8 * 3).toFloat()  )
                                }
                                3 ->{
                                    val y =  (dm.heightPixels/4).toFloat()
                                    scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/4).toFloat(), y,  (dm.widthPixels/4 * 3).toFloat(), y  )
                                }
                                4 ->{
                                    val y =  (dm.heightPixels/4).toFloat()
                                    scriptActionInfo.swipePoint = Rect( (dm.widthPixels/4 * 3).toFloat() , y,  (dm.widthPixels/4).toFloat(), y  )
                                }
                                5->{
                                    val x =  (dm.widthPixels/4 * 3).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 3).toFloat(), x,  (dm.heightPixels/8 * 5).toFloat()  )
                                }
                                6 ->{
                                    val x =  (dm.widthPixels/4 * 3).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/8 * 5).toFloat(), x,   (dm.heightPixels/8 * 3).toFloat()  )
                                }
                                7->{
                                    val y =  (dm.heightPixels/4 * 3).toFloat()
                                    scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/4).toFloat(), y,  (dm.widthPixels/4 * 3).toFloat(), y  )
                                }
                                8->{
                                    val y =  (dm.heightPixels/4 * 3).toFloat()
                                    scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/4 * 3).toFloat(), y,  (dm.widthPixels/4).toFloat() , y  )
                                }
                            }
                            scriptActionInfo.command.add(Operation(3, AdbSwipe()))
                        }
                        action.startsWith( ActionString.HOR_SWIPE ) ->{
                            val type =  action.substring(   ActionString.HOR_SWIPE.length ).toInt()
                            when(type){
                                0 -> {
                                    val x =  (dm.widthPixels/2 ).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/4).toFloat(), x,  (dm.heightPixels/2).toFloat() )
                                }
                                9 ->{
                                    val x =  (dm.widthPixels/2 ).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/2).toFloat(), x,  (dm.heightPixels/4).toFloat() )
                                }
                                1 -> {
                                    val x =  (dm.widthPixels/8 ).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/4).toFloat(), x,  (dm.heightPixels/2).toFloat() )
                                }
                                2 ->{
                                    val x =  (dm.widthPixels/8 ).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x,  (dm.heightPixels/2).toFloat() , x, (dm.heightPixels/4 ).toFloat()   )
                                }
                                3 ->{
                                    val y =  (dm.heightPixels/4).toFloat()
                                    scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/8 * 3).toFloat(), y,  (dm.widthPixels/8 * 5).toFloat(), y  )
                                }
                                4 ->{
                                    val y =  (dm.heightPixels/4).toFloat()
                                    scriptActionInfo.swipePoint = Rect( (dm.widthPixels/8 * 5).toFloat() , y,  (dm.widthPixels/8 * 3).toFloat(), y  )
                                }
                                5->{
                                    val x =  (dm.widthPixels/8 * 7).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/4 ).toFloat(), x,  (dm.heightPixels/2 ).toFloat()  )
                                }
                                6 ->{
                                    val x =  (dm.widthPixels/8 * 7).toFloat()
                                    scriptActionInfo.swipePoint = Rect( x, (dm.heightPixels/2 ).toFloat(), x,   (dm.heightPixels/4 ).toFloat()  )
                                }
                                7->{
                                    val y =  (dm.heightPixels/4 * 3).toFloat()
                                    scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/8 * 3).toFloat(), y,  (dm.widthPixels/8 * 5).toFloat(), y  )
                                }
                                8->{
                                    val y =  (dm.heightPixels/4 * 3).toFloat()
                                    scriptActionInfo.swipePoint = Rect(  (dm.widthPixels/8 * 5).toFloat(), y,  (dm.widthPixels/8 * 3).toFloat() , y  )
                                }
                            }
                            scriptActionInfo.command.add(Operation(3, AdbSwipe()))
                        }
                        action.startsWith( ActionString.UN_CHECKED )->{
                            val flowIds = action.substring( ActionString.UN_CHECKED.length+1, action.length-1).split(",").map { it.toInt() }
                            if(appDb.scriptSetInfoDao.countCheckedNumByParentFlowId(scriptActionInfo.scriptId , flowIds) > 0){
                                scriptActionInfo.addFlag = false
                                return
                            }
                        }
                        action.startsWith( ActionString.NOT_FLOWID) ->{
                            val flowId = action.substring(   ActionString.NOT_FLOWID.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(NotFlowId(flowId))
                        }
                        action.startsWith( ActionString.RM_FLOW_ID) ->{
                            val flowId = action.substring(   ActionString.RM_FLOW_ID.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(RmSkipFlowId(flowId))
                        }
                        action.startsWith( ActionString.SKIP_ACID) ->{
                            val acId = action.substring(   ActionString.SKIP_ACID.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(SkipAcId(acId))
                        }
                        action.startsWith( ActionString.RM_SKIP_ACID) ->{
                            val acId = action.substring(   ActionString.RM_SKIP_ACID.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(RmSkipAcId(acId))
                        }
                        action.startsWith( ActionString.POS_ADD) ->{
                            val saiId = action.substring(   ActionString.POS_ADD.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(AddPosById(saiId))
                        }
                        action.startsWith( ActionString.POS_MINUS) ->{
                            val saiId = action.substring(   ActionString.POS_MINUS.length+1, action.length-1   ).toInt()
                            scriptActionInfo.command.add(MinusPosById(saiId))
                        }
                        action.startsWith(ActionString.DROP_SET_NEXT) ->{
                            val setId = action.substring(ActionString.DROP_SET_NEXT.length+1, action.length-1).toInt()
                            scriptActionInfo.command.add(DropdownMenuNext(setId))
                        }
                        action.startsWith(ActionString.RM_SKIP_ACIDS) ->{
                            val acIds = mutableIntSetOf()
                            action.substring(ActionString.RM_SKIP_ACIDS.length+1, action.length-1).split(",").map {
                                acIds.add(it.toInt())
                            }
                            scriptActionInfo.command.add(RmSkipAcIdList(acIds))
                        }
                        action.startsWith(ActionString.CLICK_PERCENT) ->{
                            val pointFloat = action.substring(ActionString.CLICK_PERCENT.length+1, action.length-1).split(",").map { it.toFloat() }
                            scriptActionInfo.command.add(Operation( 2, AdbClick(
                                Point(
                                    (dm.widthPixels * pointFloat[0]).toInt(),(dm.heightPixels* pointFloat[1]).toInt()))
                                )
                            )
                        }
                        action.startsWith(ActionString.SWIPE_PERCENT) ->{
                            val pointFloat = action.substring(ActionString.SWIPE_PERCENT.length+1, action.length-1).split(",").map { it.toFloat() }
                            scriptActionInfo.swipePoint = Rect(dm.widthPixels * pointFloat[0],dm.heightPixels * pointFloat[1], dm.widthPixels * pointFloat[2], dm.heightPixels * pointFloat[3])
                            scriptActionInfo.command.add(Operation(3, AdbSwipe()))
                        }

                        action.startsWith(ActionString.RELATIVE_FIND_AND_CLICK) ->{
                            val funStr = action.substring(ActionString.RELATIVE_FIND_AND_CLICK.length+1, action.length-1).split(",").map { it.toInt() }
                            scriptActionInfo.command.add(RelFAC(funStr[0],funStr[1],funStr[2],funStr[3]))
                            scriptActionInfo.command.add(Operation( 2, AdbClick()))
                        }

                        action.startsWith(ActionString.RELATIVE_LABEL_FIND_AND_CLICK) ->{
                            val funStr = action.substring(ActionString.RELATIVE_LABEL_FIND_AND_CLICK.length+1, action.length-1).split(",").map { it.toInt() }
                            scriptActionInfo.command.add(RelLabFAC(funStr[0],funStr[1],funStr[2],funStr[3]))
                            scriptActionInfo.command.add(Operation( 2, AdbClick()))
                        }
                    }
                }
            }
        }
    }catch (_ : Exception){
        Lom.n(ERROR, "initActionFun error:${scriptActionInfo}")
        isRunning.intValue = 0
        runScope.coroutineContext.cancelChildren()
        SnackbarUtil.show("初始化action失败，请联系管理员！")
        return
    }
}