import { useEffect, useState } from 'react'
import { getOptionSnapshots } from '../../api/market'
import type { OptionSnapshot } from '../../types/market'

export function OptionsOverview() {
  const [options, setOptions] = useState<OptionSnapshot[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    async function loadOptions() {
      try {
        setOptions(await getOptionSnapshots())
      } catch {
        setError('옵션 현황을 불러오지 못했습니다.')
      }
    }

    void loadOptions()
  }, [])

  if (error) {
    return <p role="alert">{error}</p>
  }

  return (
    <section aria-labelledby="options-title">
      <h3 id="options-title">옵션 현황</h3>

      <div className="options-table-wrapper">
        <table className="options-table">
          <thead>
            <tr>
              <th scope="col">ETF</th>
              <th scope="col">30일 IV</th>
              <th scope="col">풋/콜</th>
              <th scope="col">±1σ 예상 범위</th>
              <th scope="col">지연</th>
            </tr>
          </thead>

          <tbody>
            {options.map((option) => (
              <tr key={option.ticker}>
                <th scope="row">{option.ticker}</th>
                <td>{(option.iv30Day * 100).toFixed(1)}%</td>
                <td>{option.putCallRatio.toFixed(2)}</td>
                <td>
                  ${option.expectedLow.toFixed(2)}–$
                  {option.expectedHigh.toFixed(2)}
                </td>
                <td>{option.delayMinutes / 60}시간 지연</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {options[0] && (
        <p className="data-timestamp">
          기준 시각:{' '}
          {new Date(options[0].dataAsOf).toLocaleString('ko-KR', {
            timeZone: 'Asia/Seoul',
          })}
        </p>
      )}
    </section>
  )
}
