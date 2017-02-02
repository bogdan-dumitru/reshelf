class Api::ListsController < ApiController
  before_action :set_list, only: [:show, :edit, :update, :destroy]

  def index
    @lists = current_user.lists
  end

  def show
  end

  def create
    @list = List.new(list_params.merge({user_id: current_user.id}))

    if @list.save
      render :show, status: :created
    else
      render json: @list.errors, status: :unprocessable_entity
    end
  end

  def update
    if @list.update(list_params)
      render :show, status: :ok
    else
      render json: @list.errors, status: :unprocessable_entity
    end
  end

  def destroy
    @list.destroy
    head :no_content
  end

  private

  def set_list
    @list = current_user.lists.find(params[:id])
  end

  def list_params
    params.require(:list).permit(:name, :position, :color, :book_id)
  end
end
