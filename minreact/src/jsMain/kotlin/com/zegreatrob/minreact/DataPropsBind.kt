package com.zegreatrob.minreact

abstract class DataPropsBind<T : DataProps<T>>(override val component: TMFC<T>) : DataProps<T>