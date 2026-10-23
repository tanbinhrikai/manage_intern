export function evaluatePasswordCriteria(password = '') {
  const pwd = password || ''
  return {
    minLength: pwd.length >= 8 && pwd.length <= 72,
    hasUppercase: /[A-Z]/.test(pwd),
    hasLowercase: /[a-z]/.test(pwd),
    hasNumber: /\d/.test(pwd)
  }
}

export function createPasswordStrengthValidator(t, getOldPassword = null) {
  return (rule, value, callback) => {
    if (!value) return callback()
    const criteria = evaluatePasswordCriteria(value)
    if (!criteria.minLength) {
      return callback(new Error(t('profile.rules.newPasswordLength')))
    }
    if (!criteria.hasUppercase || !criteria.hasLowercase || !criteria.hasNumber) {
      return callback(new Error(t('profile.rules.newPasswordPattern')))
    }
    const oldPwd = typeof getOldPassword === 'function' ? getOldPassword() : null
    if (oldPwd && value === oldPwd) {
      return callback(new Error(t('profile.rules.newPasswordSameAsOld')))
    }
    callback()
  }
}

