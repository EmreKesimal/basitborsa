export default function Disclaimer({ text }) {
  return (
    <p className="disclaimer">
      {text || 'Veriler gecikmeli/gün sonu olabilir. Bu platform yatırım tavsiyesi vermez.'}
    </p>
  )
}
