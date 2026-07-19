import { useState, useRef, useEffect, useCallback } from 'react';

export default function OtpInput({ length = 6, onComplete, disabled = false }) {
  const [digits, setDigits] = useState(Array(length).fill(''));
  const inputsRef = useRef([]);

  const focusInput = useCallback((index) => {
    if (index >= 0 && index < length) {
      inputsRef.current[index]?.focus();
    }
  }, [length]);

  // Notify parent when all digits filled
  useEffect(() => {
    const code = digits.join('');
    if (code.length === length && digits.every(d => d !== '')) {
      onComplete?.(code);
    }
  }, [digits, length, onComplete]);

  const handleChange = (index, value) => {
    // Only accept single digits
    const char = value.replace(/\D/g, '').slice(-1);
    if (!char && value !== '') return;

    const next = [...digits];
    next[index] = char;
    setDigits(next);

    if (char && index < length - 1) {
      focusInput(index + 1);
    }
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace') {
      if (digits[index] === '' && index > 0) {
        // Move back and clear previous
        const next = [...digits];
        next[index - 1] = '';
        setDigits(next);
        focusInput(index - 1);
      } else {
        const next = [...digits];
        next[index] = '';
        setDigits(next);
      }
      e.preventDefault();
    } else if (e.key === 'ArrowLeft') {
      focusInput(index - 1);
      e.preventDefault();
    } else if (e.key === 'ArrowRight') {
      focusInput(index + 1);
      e.preventDefault();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, length);
    if (!pasted) return;

    const next = [...digits];
    for (let i = 0; i < pasted.length; i++) {
      next[i] = pasted[i];
    }
    setDigits(next);

    // Focus last filled or next empty
    const focusIdx = Math.min(pasted.length, length - 1);
    focusInput(focusIdx);
  };

  // Reset from parent
  useEffect(() => {
    if (disabled) return;
  }, [disabled]);

  const reset = useCallback(() => {
    setDigits(Array(length).fill(''));
    focusInput(0);
  }, [length, focusInput]);

  // Expose reset
  OtpInput.reset = reset;

  return (
    <div className="otp-container" role="group" aria-label="One-time password input">
      {digits.map((digit, i) => (
        <input
          key={i}
          ref={el => inputsRef.current[i] = el}
          type="text"
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={1}
          value={digit}
          disabled={disabled}
          className={`otp-digit${digit ? ' filled' : ''}`}
          aria-label={`Digit ${i + 1} of ${length}`}
          onChange={e => handleChange(i, e.target.value)}
          onKeyDown={e => handleKeyDown(i, e)}
          onPaste={i === 0 ? handlePaste : undefined}
          onFocus={e => e.target.select()}
        />
      ))}
    </div>
  );
}
