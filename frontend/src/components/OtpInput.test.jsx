import { describe, it, expect, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import OtpInput from '../components/OtpInput';

describe('OtpInput', () => {
  let onComplete;
  let user;

  beforeEach(() => {
    onComplete = vi.fn();
    user = userEvent.setup();
  });

  it('should render 6 input boxes', () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    expect(inputs).toHaveLength(6);
  });

  it('should have aria labels on each digit', () => {
    render(<OtpInput onComplete={onComplete} />);
    for (let i = 1; i <= 6; i++) {
      expect(screen.getByLabelText(`Digit ${i} of 6`)).toBeInTheDocument();
    }
  });

  it('should accept a numeric digit and auto-advance to next box', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.keyboard('1');
    expect(inputs[0]).toHaveValue('1');
    expect(inputs[1]).toHaveFocus();
  });

  it('should reject non-numeric input', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.keyboard('a');
    expect(inputs[0]).toHaveValue('');
  });

  it('should handle backspace to clear current digit', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.keyboard('1');
    expect(inputs[0]).toHaveValue('1');
    await user.click(inputs[0]);
    await user.keyboard('{Backspace}');
    expect(inputs[0]).toHaveValue('');
  });

  it('should move focus back on backspace when current digit is empty', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.keyboard('12');
    expect(inputs[2]).toHaveFocus();
    await user.keyboard('{Backspace}');
    expect(inputs[1]).toHaveFocus();
    expect(inputs[1]).toHaveValue('');
  });

  it('should support pasting a full 6-digit OTP', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.paste('123456');
    expect(inputs[0]).toHaveValue('1');
    expect(inputs[1]).toHaveValue('2');
    expect(inputs[2]).toHaveValue('3');
    expect(inputs[3]).toHaveValue('4');
    expect(inputs[4]).toHaveValue('5');
    expect(inputs[5]).toHaveValue('6');
  });

  it('should strip non-numeric characters from pasted text', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.paste('12ab56');
    // Should paste only numeric: 1, 2, 5, 6
    expect(inputs[0]).toHaveValue('1');
    expect(inputs[1]).toHaveValue('2');
    expect(inputs[2]).toHaveValue('5');
    expect(inputs[3]).toHaveValue('6');
  });

  it('should call onComplete when all 6 digits are filled', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.keyboard('123456');
    expect(onComplete).toHaveBeenCalledWith('123456');
  });

  it('should not call onComplete when not all digits are filled', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.keyboard('12345');
    expect(onComplete).not.toHaveBeenCalled();
  });

  it('should disable all inputs when disabled prop is true', () => {
    render(<OtpInput onComplete={onComplete} disabled={true} />);
    const inputs = screen.getAllByRole('textbox');
    inputs.forEach(input => expect(input).toBeDisabled());
  });

  it('should apply filled class when digit has value', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.keyboard('5');
    expect(inputs[0]).toHaveClass('filled');
  });

  it('should not apply filled class when digit is empty', () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    expect(inputs[0]).not.toHaveClass('filled');
  });

  it('should have inputMode=numeric for mobile keyboards', () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    inputs.forEach(input => expect(input).toHaveAttribute('inputMode', 'numeric'));
  });

  it('should navigate with arrow keys', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[2]);
    await user.keyboard('{ArrowLeft}');
    expect(inputs[1]).toHaveFocus();
    await user.keyboard('{ArrowRight}');
    expect(inputs[2]).toHaveFocus();
  });

  it('should truncate pasted text longer than 6 digits', async () => {
    render(<OtpInput onComplete={onComplete} />);
    const inputs = screen.getAllByRole('textbox');
    await user.click(inputs[0]);
    await user.paste('12345678');
    expect(inputs[5]).toHaveValue('6');
  });
});
