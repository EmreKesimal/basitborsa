import LottieAnimation from './LottieAnimation.jsx'

export default function LoadingState({ message = 'Yükleniyor...', lottie }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-4 text-on-surface-variant">
      {lottie ? (
        <LottieAnimation src={lottie} className="w-36 h-20" loop />
      ) : (
        <div className="w-8 h-8 border-4 border-surface-container-high border-t-primary rounded-full animate-spin" />
      )}
      <p className="text-body-md">{message}</p>
    </div>
  )
}
