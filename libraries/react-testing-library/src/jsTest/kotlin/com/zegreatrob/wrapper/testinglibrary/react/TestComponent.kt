package com.zegreatrob.wrapper.testinglibrary.react

import react.FC
import react.Props
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.span

val TestComponent = FC<Props> {
    div {
        div {
            h1 { +"Testing Library Test" }
            span {
                div {
                    h2 { +"Awesome button section" }
                    label {
                        +"Press Me"
                        button {
                            asDynamic()["data-testid"] = "Awesome-testId"
                            asDynamic()["data-test-info"] = "pretty-cool"
                            +"Awesome"
                        }
                    }
                }
            }
            div {
                h2 { +"Cool button section" }
                span {
                    label {
                        +"Chill"
                        button {
                            asDynamic()["data-testid"] = "Cool-testId"
                            asDynamic()["data-test-info"] = "pretty-cool"
                            +"Cool"
                        }
                    }
                    label {
                        +"Chill"
                        button {
                            asDynamic()["data-testid"] = "Cool-testId"
                            asDynamic()["data-test-info"] = "very-cool"
                            +"Cool"
                        }
                    }
                    label {
                        +"Chill"
                        button {
                            asDynamic()["data-testid"] = "Cool-testId"
                            asDynamic()["data-test-info"] = "extremely-cool"
                            +"Cool"
                        }
                    }
                }
            }
        }
    }
}
