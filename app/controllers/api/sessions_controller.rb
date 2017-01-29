class Api::SessionsController < ApiController
  skip_before_action :authorize!, only: :signup

  def signin
    @user = current_user
    render template: 'api/users/show'
  end

  def signup
    @user = User.create!(user_params)
    render template: 'api/users/show'
  end

  private

  def user_params
    params.require(:user).permit(:name)
  end
end
