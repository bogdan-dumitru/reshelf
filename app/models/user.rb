class User < ApplicationRecord
  has_many :lists

  after_create do |user|
    user.update!(token: SecureRandom.hex)
  end
end
