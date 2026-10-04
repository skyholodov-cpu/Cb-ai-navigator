package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.consultant.parseStructuredReport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AI Navigator", appName)
    }

    @Test
    fun `parseStructuredReport extracts 5 standard sections correctly`() {
        val sampleResponse = """
### 1. Краткий вывод
Перевес на стороне продолжения восходящего тренда в технологическом секторе.

### 2. Детальный анализ
Мультипликаторы находятся на приемлемом уровне при сохранении темпов роста свободного денежного потока.

### 3. Риски и неопределённости
Ключевой фактор риска — волатильность процентных ставок.

### 4. Возможные сценарии / альтернативы
Базовый сценарий предполагает умеренный рост, альтернативный — коррекцию к поддержке.

### 5. Практические следующие шаги
Отслеживать квартальный отчёт и объёмы торгов на пробое уровня.
        """.trimIndent()

        val sections = parseStructuredReport(sampleResponse)
        assertEquals(5, sections.size)
        assertTrue(sections[0].title.contains("Краткий вывод", ignoreCase = true))
        assertTrue(sections[1].title.contains("Детальный анализ", ignoreCase = true))
        assertTrue(sections[2].title.contains("Риски", ignoreCase = true))
        assertTrue(sections[3].title.contains("сценарии", ignoreCase = true))
        assertTrue(sections[4].title.contains("Практические", ignoreCase = true))
    }
}
